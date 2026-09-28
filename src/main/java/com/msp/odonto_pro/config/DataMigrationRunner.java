package com.msp.odonto_pro.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.msp.odonto_pro.entity.clinica.Clinica;
import com.msp.odonto_pro.entity.convenio.Convenio;
import com.msp.odonto_pro.entity.dentista.Dentista;
import com.msp.odonto_pro.entity.financeiro.Despesa;
import com.msp.odonto_pro.entity.estoque.ItemEstoque;
import com.msp.odonto_pro.entity.orcamento.Orcamento;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.enums.Role;
import com.msp.odonto_pro.repository.clinica.ClinicaRepository;
import com.msp.odonto_pro.repository.convenio.ConvenioRepository;
import com.msp.odonto_pro.repository.dentista.DentistaRepository;
import com.msp.odonto_pro.repository.financeiro.DespesaRepository;
import com.msp.odonto_pro.repository.estoque.ItemEstoqueRepository;
import com.msp.odonto_pro.repository.orcamento.OrcamentoRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.repository.usuario.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Migração one-shot para a feature multi-clínica: 1. Todo registro
 * pré-existente (usuários, pacientes, dentistas, convênios, itens de
 * estoque, despesas) sem clinicaId é associado a uma "Clínica Padrão"
 * criada automaticamente — os demais recursos (consultas, orçamentos,
 * prontuário etc.) são filtrados transitivamente através desses, sem
 * precisar de coluna própria. 2. Garante que existe ao menos um usuário
 * SUPER_ADMIN para operar o cadastro de clínicas.
 *
 * Idempotente: em execuções seguintes, não há mais registros com clinicaId
 * nulo (exceto SUPER_ADMIN, que nunca deve ter) nem falta de SUPER_ADMIN, e
 * os blocos abaixo viram no-op.
 */
@Component
@RequiredArgsConstructor
public class DataMigrationRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataMigrationRunner.class);
	private static final String CLINICA_PADRAO_NOME = "Clínica Padrão";

	private final ClinicaRepository clinicaRepository;
	private final UsuarioRepository usuarioRepository;
	private final PacienteRepository pacienteRepository;
	private final DentistaRepository dentistaRepository;
	private final ConvenioRepository convenioRepository;
	private final ItemEstoqueRepository itemEstoqueRepository;
	private final DespesaRepository despesaRepository;
	private final OrcamentoRepository orcamentoRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.super-admin.username:superadmin}")
	private String superAdminUsername;

	@Value("${app.super-admin.email:superadmin@odontopro.local}")
	private String superAdminEmail;

	/** Sem valor padrão de propósito: se não vier de env var, geramos uma senha aleatória e a logamos uma única vez. */
	@Value("${app.super-admin.password:#{null}}")
	private String superAdminPasswordConfigurada;

	@Override
	@Transactional
	public void run(final String... args) {
		final Long clinicaPadraoId = this.migrarUsuariosSemClinica();
		this.garantirSuperAdmin();

		if (clinicaPadraoId == null) {
			// Ninguém sem clínica (instalação nova ou migração já rodou antes) —
			// nada pra retroagir nos demais recursos.
			return;
		}

		this.migrarSemClinica("paciente", this.pacienteRepository, Paciente::getClinicaId, Paciente::setClinicaId,
				clinicaPadraoId);
		this.migrarSemClinica("dentista", this.dentistaRepository, Dentista::getClinicaId, Dentista::setClinicaId,
				clinicaPadraoId);
		this.migrarSemClinica("convenio", this.convenioRepository, Convenio::getClinicaId, Convenio::setClinicaId,
				clinicaPadraoId);
		this.migrarSemClinica("item de estoque", this.itemEstoqueRepository, ItemEstoque::getClinicaId,
				ItemEstoque::setClinicaId, clinicaPadraoId);
		this.migrarSemClinica("despesa", this.despesaRepository, Despesa::getClinicaId, Despesa::setClinicaId,
				clinicaPadraoId);

		this.migrarOrcamentosSemLinkToken();
	}

	/** Orçamentos criados antes do link público de aprovação (ver Orcamento.linkToken) ganham um token retroativamente. */
	private void migrarOrcamentosSemLinkToken() {
		final List<Orcamento> semToken = this.orcamentoRepository.findAll().stream()
				.filter(o -> o.getLinkToken() == null).collect(Collectors.toList());

		if (semToken.isEmpty()) {
			return;
		}

		semToken.forEach(o -> o.setLinkToken(UUID.randomUUID().toString()));
		this.orcamentoRepository.saveAll(semToken);

		log.info("[migracao orcamento] {} orcamento(s) pre-existente(s) ganharam linkToken retroativamente",
				semToken.size());
	}

	/** @return id da "Clínica Padrão" (existente ou recém-criada), ou null se não havia ninguém pra migrar. */
	private Long migrarUsuariosSemClinica() {
		final List<Usuario> semClinica = this.usuarioRepository.findAll().stream()
				.filter(u -> u.getClinicaId() == null && u.getRole() != Role.SUPER_ADMIN)
				.collect(Collectors.toList());

		if (semClinica.isEmpty()) {
			return this.clinicaRepository.findFirstByNome(CLINICA_PADRAO_NOME).map(Clinica::getId).orElse(null);
		}

		final Clinica padrao = this.clinicaRepository.findFirstByNome(CLINICA_PADRAO_NOME)
				.orElseGet(() -> this.clinicaRepository.save(Clinica.builder().nome(CLINICA_PADRAO_NOME).ativo(true).build()));

		semClinica.forEach(u -> u.setClinicaId(padrao.getId()));
		this.usuarioRepository.saveAll(semClinica);

		log.info("[migracao multi-clinica] {} usuario(s) pre-existente(s) associado(s) a '{}' (id={})",
				semClinica.size(), CLINICA_PADRAO_NOME, padrao.getId());

		return padrao.getId();
	}

	private <T> void migrarSemClinica(final String nomeEntidade, final JpaRepository<T, ?> repository,
			final Function<T, Long> getClinicaId, final BiConsumer<T, Long> setClinicaId, final Long clinicaPadraoId) {
		final List<T> semClinica = repository.findAll().stream().filter(r -> getClinicaId.apply(r) == null)
				.collect(Collectors.toList());

		if (semClinica.isEmpty()) {
			return;
		}

		semClinica.forEach(r -> setClinicaId.accept(r, clinicaPadraoId));
		repository.saveAll(semClinica);

		log.info("[migracao multi-clinica] {} {}(s) pre-existente(s) associado(s) a '{}' (id={})", semClinica.size(),
				nomeEntidade, CLINICA_PADRAO_NOME, clinicaPadraoId);
	}

	private void garantirSuperAdmin() {
		final boolean existeSuperAdmin = this.usuarioRepository.findAll().stream()
				.anyMatch(u -> u.getRole() == Role.SUPER_ADMIN);
		if (existeSuperAdmin) {
			return;
		}

		if (this.usuarioRepository.existsByUsername(this.superAdminUsername)) {
			log.warn("[migracao multi-clinica] nao ha SUPER_ADMIN, mas o username '{}' ja esta em uso por outro "
					+ "papel; defina app.super-admin.username para um valor livre e reinicie a aplicacao.",
					this.superAdminUsername);
			return;
		}

		final boolean senhaGerada = this.superAdminPasswordConfigurada == null;
		final String senha = senhaGerada ? UUID.randomUUID().toString().substring(0, 12)
				: this.superAdminPasswordConfigurada;

		final Usuario superAdmin = new Usuario();
		superAdmin.setNome("Super Administrador");
		superAdmin.setUsername(this.superAdminUsername);
		superAdmin.setEmail(this.superAdminEmail);
		superAdmin.setPassword(this.passwordEncoder.encode(senha));
		superAdmin.setRole(Role.SUPER_ADMIN);
		superAdmin.setAtivo(true);
		this.usuarioRepository.save(superAdmin);

		// A senha gerada nunca vai para o log (log de aplicacao costuma ser agregado/
		// retido bem alem do necessario) — escrevemos uma unica vez num arquivo local
		// git-ignorado, que o operador deve apagar apos o primeiro login.
		if (senhaGerada) {
			this.escreverCredencialInicial(this.superAdminUsername, senha);
			log.warn("=== SUPER_ADMIN inicial criado: username='{}' — senha gerada automaticamente, veja "
					+ "super-admin-credentials.txt na raiz do projeto (apague o arquivo apos o primeiro login; "
					+ "para trocar a senha use POST /api/v1/auth/alterar-senha) ===", this.superAdminUsername);
		} else {
			log.warn("=== SUPER_ADMIN inicial criado: username='{}' (senha definida via app.super-admin.password) ===",
					this.superAdminUsername);
		}
	}

	/** Escreve a senha gerada uma única vez; não sobrescreve se o arquivo já existir de uma execução anterior. */
	private void escreverCredencialInicial(final String username, final String senha) {
		final Path arquivo = Paths.get("super-admin-credentials.txt");
		if (Files.exists(arquivo)) {
			return;
		}
		try {
			Files.writeString(arquivo, "username=" + username + "\nsenha=" + senha + "\n"
					+ "Apague este arquivo depois do primeiro login. Para trocar a senha, use POST /api/v1/auth/alterar-senha.\n");
		} catch (final IOException e) {
			log.error("Nao foi possivel escrever super-admin-credentials.txt; a senha gerada foi perdida — "
					+ "delete o usuario '{}' do banco e reinicie a aplicacao para gerar outra.", username, e);
		}
	}
}
