package com.msp.odonto_pro.service.paciente;
import com.msp.odonto_pro.service.termoconsentimento.TermoConsentimentoService;
import com.msp.odonto_pro.service.prontuario.ProntuarioService;
import com.msp.odonto_pro.service.orcamento.OrcamentoService;
import com.msp.odonto_pro.service.odontograma.OdontogramaService;
import com.msp.odonto_pro.service.evolucaoclinica.EvolucaoClinicaService;
import com.msp.odonto_pro.service.anexoclinico.AnexoClinicoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.msp.odonto_pro.dto.financeiro.PagamentoDTO;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.financeiro.Pagamento;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.repository.financeiro.PagamentoRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * Direitos do titular (LGPD) sobre os dados de um paciente: exportação
 * completa e anonimização.
 *
 * A anonimização atua sobre os campos pessoais do próprio {@link Paciente}
 * (nome, CPF, contatos, endereço, foto). Registros vinculados
 * (prontuário/evoluções/anexos/orçamentos/pagamentos) são preservados —
 * conforme obrigações legais de guarda de prontuário e registros
 * financeiros — e deixam de expor identificação direta porque o paciente ao
 * qual se referem já está anonimizado. Texto livre (observações, anamnese,
 * evoluções) não é varrido em busca de PII: é responsabilidade de quem
 * registrou não incluir dados de outra pessoa ali.
 */
@Service
@RequiredArgsConstructor
public class PacienteLgpdService {

	private final PacienteRepository pacienteRepository;
	private final PacienteService pacienteService;
	private final ProntuarioService prontuarioService;
	private final OdontogramaService odontogramaService;
	private final OrcamentoService orcamentoService;
	private final AnexoClinicoService anexoClinicoService;
	private final TermoConsentimentoService termoConsentimentoService;
	private final EvolucaoClinicaService evolucaoClinicaService;
	private final PagamentoRepository pagamentoRepository;

	/**
	 * Instância própria (não injetada): este setup não expõe um bean
	 * ObjectMapper autoconfigurado (spring-boot-starter-webmvc sem
	 * JacksonAutoConfiguration de bean). findAndRegisterModules() detecta o
	 * jackson-datatype-jsr310 via ServiceLoader, cobrindo LocalDate/LocalDateTime.
	 */
	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	@Transactional(readOnly = true)
	public byte[] exportarDados(final Long pacienteId) throws IOException {
		this.exigir(pacienteId);

		final Map<String, Object> export = new LinkedHashMap<>();
		export.put("cadastro", this.pacienteService.buscarPorId(pacienteId));
		export.put("prontuario", this.prontuarioService.obter(pacienteId));
		export.put("odontograma", this.odontogramaService.listar(pacienteId));
		export.put("orcamentos", this.orcamentoService.listar(pacienteId));
		export.put("anexos", this.anexoClinicoService.listar(pacienteId));
		export.put("termosConsentimento", this.termoConsentimentoService.listar(pacienteId));
		export.put("evolucoesClinicas", this.evolucaoClinicaService.listar(pacienteId));
		export.put("pagamentos", this.listarPagamentos(pacienteId));
		export.put("exportadoEm", LocalDateTime.now());

		return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(export);
	}

	@Transactional
	public void anonimizarDadosPessoais(final Long pacienteId) {
		final Paciente paciente = this.exigir(pacienteId);

		if (paciente.getFotoUrl() != null) {
			this.tentarExcluirArquivo(paciente.getFotoUrl());
		}

		paciente.setNome("Paciente Anonimizado #" + paciente.getId());
		paciente.setCpf(null);
		paciente.setTelefone(null);
		paciente.setEmail(null);
		paciente.setEndereco(null);
		paciente.setSexo(null);
		paciente.setDataNascimento(null);
		paciente.setNumeroCarteirinha(null);
		paciente.setResponsavelNome(null);
		paciente.setResponsavelCpf(null);
		paciente.setContatoEmergenciaNome(null);
		paciente.setContatoEmergenciaTelefone(null);
		paciente.setObservacoes(null);
		paciente.setCep(null);
		paciente.setLogradouro(null);
		paciente.setNumero(null);
		paciente.setComplemento(null);
		paciente.setBairro(null);
		paciente.setCidade(null);
		paciente.setUf(null);
		paciente.setFotoUrl(null);
		paciente.setDadosAnonimizados(true);
		paciente.setDadosAnonimizadosEm(LocalDateTime.now());

		this.pacienteRepository.save(paciente);
	}

	private List<PagamentoDTO> listarPagamentos(final Long pacienteId) {
		return this.pagamentoRepository.findByPacienteId(pacienteId).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	private PagamentoDTO toDto(final Pagamento pagamento) {
		final var item = pagamento.getItemTratamento();
		return PagamentoDTO.builder().id(pagamento.getId()).itemTratamentoId(item.getId())
				.procedimento(item.getProcedimento()).valor(pagamento.getValor())
				.formaPagamento(pagamento.getFormaPagamento()).data(pagamento.getData())
				.observacao(pagamento.getObservacao()).criadoEm(pagamento.getCriadoEm()).build();
	}

	private void tentarExcluirArquivo(final String urlRelativa) {
		try {
			final Path caminho = Paths.get(urlRelativa.startsWith("/") ? urlRelativa.substring(1) : urlRelativa);
			Files.deleteIfExists(caminho);
		} catch (final IOException ignored) {
			// Best-effort: a anonimização dos dados no banco não deve falhar por
			// causa de um arquivo que não pôde ser removido do disco.
		}
	}

	private Paciente exigir(final Long id) {
		final Paciente paciente = this.pacienteRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + id + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(paciente.getClinicaId())) {
			throw new NotFoundException("Paciente com id: " + id + " não encontrado!");
		}
		return paciente;
	}
}
