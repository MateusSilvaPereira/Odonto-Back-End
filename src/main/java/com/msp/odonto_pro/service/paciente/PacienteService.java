package com.msp.odonto_pro.service.paciente;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.msp.odonto_pro.dto.paciente.PacienteDTO;
import com.msp.odonto_pro.entity.convenio.Convenio;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.dto.paciente.PacienteProjection;
import com.msp.odonto_pro.repository.convenio.ConvenioRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.security.SecurityUtils;
import com.msp.odonto_pro.util.ArquivoUploadUtil;
import com.msp.odonto_pro.util.PdfReportUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PacienteService {

	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final PacienteRepository pacienteRepository;
	private final ConvenioRepository convenioRepository;

	public PacienteDTO save(final PacienteDTO dto) {
		return this.salvar(dto);
	}

	// PUT /pacientes/{id}: o id vem da URL, não mais do corpo — evita
	// confiar num id arbitrário que o cliente decida colocar no payload.
	public PacienteDTO atualizar(final Long id, final PacienteDTO dto) {
		dto.setId(id);
		return this.salvar(dto);
	}

	private PacienteDTO salvar(final PacienteDTO dto) {
		final Long clinicaId = SecurityUtils.clinicaIdAtual();

		// Em edição, garante que o paciente é da própria clínica ANTES de
		// aceitar qualquer alteração (nunca confia no payload pra isso).
		final Paciente existente = dto.getId() != null ? this.exigir(dto.getId()) : null;

		final Paciente paciente = new Paciente();
		paciente.setId(dto.getId());
		paciente.setClinicaId(clinicaId);
		paciente.setNome(dto.getNome());
		paciente.setCpf(dto.getCpf());
		paciente.setTelefone(dto.getTelefone());
		paciente.setEmail(dto.getEmail());
		paciente.setDataNascimento(dto.getDataNascimento());
		paciente.setEndereco(dto.getEndereco());
		paciente.setSexo(dto.getSexo());
		paciente.setConvenio(this.resolverConvenio(dto.getConvenioId()));
		paciente.setNumeroCarteirinha(dto.getNumeroCarteirinha());
		paciente.setResponsavelNome(dto.getResponsavelNome());
		paciente.setResponsavelCpf(dto.getResponsavelCpf());
		paciente.setContatoEmergenciaNome(dto.getContatoEmergenciaNome());
		paciente.setContatoEmergenciaTelefone(dto.getContatoEmergenciaTelefone());
		paciente.setObservacoes(dto.getObservacoes());
		paciente.setCep(dto.getCep());
		paciente.setLogradouro(dto.getLogradouro());
		paciente.setNumero(dto.getNumero());
		paciente.setComplemento(dto.getComplemento());
		paciente.setBairro(dto.getBairro());
		paciente.setCidade(dto.getCidade());
		paciente.setUf(dto.getUf());
		paciente.setCriadoEm(dto.getCriadoEm());
		paciente.setConsultas(dto.getConsultas());
		paciente.setConsentimentoLgpd(dto.getConsentimentoLgpd());
		paciente.setConsentimentoLgpdEm(
				Boolean.TRUE.equals(dto.getConsentimentoLgpd()) && dto.getConsentimentoLgpdEm() == null
						? java.time.LocalDateTime.now()
						: dto.getConsentimentoLgpdEm());

		// fotoUrl, o estado de anonimização (LGPD) e o último recall são
		// geridos por endpoints dedicados; preserva os valores atuais pra um
		// "salvar" comum não apagar a foto, reverter uma anonimização, nem
		// zerar o histórico de recall.
		if (existente != null) {
			paciente.setFotoUrl(existente.getFotoUrl());
			paciente.setDadosAnonimizados(existente.isDadosAnonimizados());
			paciente.setDadosAnonimizadosEm(existente.getDadosAnonimizadosEm());
			paciente.setUltimoRecallEnviadoEm(existente.getUltimoRecallEnviadoEm());
		}

		return this.toDto(this.pacienteRepository.save(paciente));
	}

	public PacienteDTO atualizarFoto(final Long id, final MultipartFile foto) throws IOException {
		final Paciente paciente = this.exigir(id);
		if (foto == null || foto.isEmpty()) {
			throw new IllegalArgumentException("foto é obrigatória");
		}
		paciente.setFotoUrl(ArquivoUploadUtil.salvar(foto, "fotos-pacientes"));
		return this.toDto(this.pacienteRepository.save(paciente));
	}

	public PacienteDTO removerFoto(final Long id) {
		final Paciente paciente = this.exigir(id);
		paciente.setFotoUrl(null);
		return this.toDto(this.pacienteRepository.save(paciente));
	}

	public PacienteDTO buscarPorId(final Long id) {
		return this.toDto(this.exigir(id));
	}

	private Paciente exigir(final Long id) {
		final Paciente paciente = this.pacienteRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + id + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(paciente.getClinicaId())) {
			throw new NotFoundException("Paciente com id: " + id + " não encontrado!");
		}
		return paciente;
	}

	private Convenio resolverConvenio(final Long convenioId) {
		if (convenioId == null) {
			return null;
		}
		if (!this.convenioRepository.existsByIdAndClinicaId(convenioId, SecurityUtils.clinicaIdAtual())) {
			throw new NotFoundException("Convênio com id: " + convenioId + " não encontrado!");
		}
		return this.convenioRepository.findById(convenioId).orElseThrow();
	}

	// Deletar Paciente...
	public void excluir(Long id) {
		this.exigir(id);
		this.pacienteRepository.deleteById(id);
	}

	// Listar pacientes
	public Page<PacienteProjection> listarPacientesPaginado(Integer pagina, Integer quantidade) {
		Sort sort = Sort.by("nome").ascending();
		PageRequest pageRequest = PageRequest.of(pagina - 1, quantidade, sort);
		return pacienteRepository.listarPacientes(SecurityUtils.clinicaIdAtual(), pageRequest);
	}

	// Listar todos os pacientes, sem paginação (usado na tela de Relatórios)
	public List<PacienteDTO> listarTodos() {
		return this.pacienteRepository.findByClinicaId(SecurityUtils.clinicaIdAtual()).stream()
				.sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome())).map(this::toDto)
				.collect(Collectors.toList());
	}

	// Gera um PDF com a listagem completa de pacientes
	public byte[] gerarRelatorioPdf() {
		final List<Paciente> pacientes = this.pacienteRepository.findByClinicaId(SecurityUtils.clinicaIdAtual())
				.stream().sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome())).collect(Collectors.toList());

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			final Document document = PdfReportUtil.novoDocumento(out);
			document.add(PdfReportUtil.titulo("Relatório de Pacientes"));
			document.add(new com.lowagie.text.Paragraph(" "));

			final PdfPTable table = PdfReportUtil.novaTabela(new float[] { 3f, 2f, 2f, 2f, 3f }, "Nome", "CPF",
					"Telefone", "Nascimento", "Email");

			for (final Paciente paciente : pacientes) {
				PdfReportUtil.celula(table, paciente.getNome());
				PdfReportUtil.celula(table, paciente.getCpf());
				PdfReportUtil.celula(table, paciente.getTelefone());
				PdfReportUtil.celula(table,
						paciente.getDataNascimento() != null ? paciente.getDataNascimento().format(DATA_BR) : "");
				PdfReportUtil.celula(table, paciente.getEmail());
			}

			document.add(table);
			document.close();
			return out.toByteArray();
		} catch (final DocumentException | java.io.IOException e) {
			throw new RuntimeException("Erro ao gerar relatório de pacientes", e);
		}
	}

	private PacienteDTO toDto(final Paciente paciente) {
		return PacienteDTO.builder().id(paciente.getId()).nome(paciente.getNome()).cpf(paciente.getCpf())
				.telefone(paciente.getTelefone()).email(paciente.getEmail())
				.dataNascimento(paciente.getDataNascimento()).endereco(paciente.getEndereco())
				.sexo(paciente.getSexo())
				.convenioId(paciente.getConvenio() != null ? paciente.getConvenio().getId() : null)
				.convenio(paciente.getNomeConvenio())
				.numeroCarteirinha(paciente.getNumeroCarteirinha()).responsavelNome(paciente.getResponsavelNome())
				.responsavelCpf(paciente.getResponsavelCpf())
				.contatoEmergenciaNome(paciente.getContatoEmergenciaNome())
				.contatoEmergenciaTelefone(paciente.getContatoEmergenciaTelefone())
				.observacoes(paciente.getObservacoes()).cep(paciente.getCep()).logradouro(paciente.getLogradouro())
				.numero(paciente.getNumero()).complemento(paciente.getComplemento()).bairro(paciente.getBairro())
				.cidade(paciente.getCidade()).uf(paciente.getUf()).fotoUrl(paciente.getFotoUrl())
				.criadoEm(paciente.getCriadoEm()).consentimentoLgpd(paciente.getConsentimentoLgpd())
				.consentimentoLgpdEm(paciente.getConsentimentoLgpdEm())
				.dadosAnonimizados(paciente.isDadosAnonimizados())
				.dadosAnonimizadosEm(paciente.getDadosAnonimizadosEm())
				.ultimoRecallEnviadoEm(paciente.getUltimoRecallEnviadoEm()).build();
	}

}
