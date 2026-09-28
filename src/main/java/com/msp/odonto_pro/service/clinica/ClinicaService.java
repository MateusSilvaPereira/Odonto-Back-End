package com.msp.odonto_pro.service.clinica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.msp.odonto_pro.dto.clinica.ClinicaCreateRequest;
import com.msp.odonto_pro.dto.clinica.ClinicaDTO;
import com.msp.odonto_pro.entity.clinica.Clinica;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.enums.Role;
import com.msp.odonto_pro.exceptions.ConflitoException;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.clinica.ClinicaRepository;
import com.msp.odonto_pro.repository.usuario.UsuarioRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Gerenciamento de clínicas (tenants). Os endpoints de listagem/criação/
 * edição/exclusão por id são de uso exclusivo do SUPER_ADMIN (ver
 * ClinicaController); os métodos "minha clínica" são usados pelo ADMIN da
 * própria clínica, derivada do token via {@link SecurityUtils}.
 */
@Service
@RequiredArgsConstructor
public class ClinicaService {

	private static final String DIRETORIO_UPLOAD = "uploads/clinica/";

	private final ClinicaRepository clinicaRepository;
	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public List<ClinicaDTO> listar() {
		return this.clinicaRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
	}

	@Transactional
	public ClinicaDTO criar(final ClinicaCreateRequest request) {
		if (request.getNome() == null || request.getNome().isBlank()) {
			throw new IllegalArgumentException("nome da clínica é obrigatório");
		}
		if (request.getAdminUsername() == null || request.getAdminEmail() == null
				|| request.getAdminPassword() == null || request.getAdminNome() == null) {
			throw new IllegalArgumentException("dados do administrador inicial (adminNome, adminUsername, "
					+ "adminEmail, adminPassword) são obrigatórios");
		}
		if (request.getCnpj() != null && !request.getCnpj().isBlank()
				&& this.clinicaRepository.existsByCnpj(request.getCnpj())) {
			throw new ConflitoException("Já existe uma clínica com este CNPJ");
		}
		if (this.usuarioRepository.existsByUsername(request.getAdminUsername())) {
			throw new ConflitoException("Username já existe");
		}
		if (this.usuarioRepository.existsByEmail(request.getAdminEmail())) {
			throw new ConflitoException("E-mail já cadastrado");
		}

		final Clinica clinica = Clinica.builder().nome(request.getNome()).cnpj(request.getCnpj())
				.telefone(request.getTelefone()).endereco(request.getEndereco()).ativo(true).build();
		final Clinica salva = this.clinicaRepository.save(clinica);

		final Usuario admin = new Usuario();
		admin.setNome(request.getAdminNome());
		admin.setUsername(request.getAdminUsername());
		admin.setEmail(request.getAdminEmail());
		admin.setPassword(this.passwordEncoder.encode(request.getAdminPassword()));
		admin.setRole(Role.ADMIN);
		admin.setAtivo(true);
		admin.setClinicaId(salva.getId());
		this.usuarioRepository.save(admin);

		return this.toDto(salva);
	}

	public ClinicaDTO atualizar(final Long id, final String nome, final String cnpj, final String telefone,
			final String endereco) {
		final Clinica clinica = this.buscar(id);
		this.aplicarCampos(clinica, nome, cnpj, telefone, endereco);
		return this.toDto(this.clinicaRepository.save(clinica));
	}

	public ClinicaDTO toggleActive(final Long id) {
		final Clinica clinica = this.buscar(id);
		clinica.setAtivo(!clinica.isAtivo());
		return this.toDto(this.clinicaRepository.save(clinica));
	}

	public void deletar(final Long id) {
		if (!this.clinicaRepository.existsById(id)) {
			throw new NotFoundException("Clínica não encontrada");
		}
		this.clinicaRepository.deleteById(id);
	}

	public ClinicaDTO obterMinha() {
		return this.toDto(this.buscar(SecurityUtils.clinicaIdAtual()));
	}

	public ClinicaDTO atualizarMinha(final String nome, final String cnpj, final String telefone,
			final String endereco, final MultipartFile logoFile) throws IOException {
		final Clinica clinica = this.buscar(SecurityUtils.clinicaIdAtual());
		this.aplicarCampos(clinica, nome, cnpj, telefone, endereco);

		if (logoFile != null && !logoFile.isEmpty()) {
			clinica.setLogoUrl(this.salvarLogo(logoFile));
		}

		return this.toDto(this.clinicaRepository.save(clinica));
	}

	private void aplicarCampos(final Clinica clinica, final String nome, final String cnpj, final String telefone,
			final String endereco) {
		if (nome == null || nome.isBlank()) {
			throw new IllegalArgumentException("nome é obrigatório");
		}
		clinica.setNome(nome);
		clinica.setCnpj(cnpj);
		clinica.setTelefone(telefone);
		clinica.setEndereco(endereco);
	}

	private Clinica buscar(final Long id) {
		return this.clinicaRepository.findById(id).orElseThrow(() -> new NotFoundException("Clínica não encontrada"));
	}

	private String salvarLogo(final MultipartFile logoFile) throws IOException {
		Files.createDirectories(Paths.get(DIRETORIO_UPLOAD));

		final String nomeArmazenado = UUID.randomUUID() + "_" + logoFile.getOriginalFilename();
		final Path caminho = Paths.get(DIRETORIO_UPLOAD, nomeArmazenado);
		Files.write(caminho, logoFile.getBytes());

		return "/" + DIRETORIO_UPLOAD + nomeArmazenado;
	}

	private ClinicaDTO toDto(final Clinica clinica) {
		return ClinicaDTO.builder().id(clinica.getId()).nome(clinica.getNome()).cnpj(clinica.getCnpj())
				.telefone(clinica.getTelefone()).endereco(clinica.getEndereco()).logoUrl(clinica.getLogoUrl())
				.ativo(clinica.isAtivo()).criadoEm(clinica.getCriadoEm()).build();
	}
}
