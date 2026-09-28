package com.msp.odonto_pro.service.termoconsentimento;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.termoconsentimento.TermoConsentimentoDTO;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.termoconsentimento.TermoConsentimento;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.repository.termoconsentimento.TermoConsentimentoRepository;
import com.msp.odonto_pro.security.SecurityUtils;
import com.msp.odonto_pro.util.ArquivoUploadUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TermoConsentimentoService {

	private static final String DIRETORIO_ASSINATURAS = "assinaturas";

	private final TermoConsentimentoRepository termoConsentimentoRepository;
	private final PacienteRepository pacienteRepository;

	public List<TermoConsentimentoDTO> listar(final Long pacienteId) {
		this.exigirPaciente(pacienteId);
		return this.termoConsentimentoRepository.findByPacienteIdOrderByAssinadoEmDesc(pacienteId).stream()
				.map(this::toDto).collect(Collectors.toList());
	}

	public TermoConsentimentoDTO registrar(final Long pacienteId, final TermoConsentimentoDTO dto,
			final Usuario usuarioLogado) throws IOException {
		final Paciente paciente = this.exigirPaciente(pacienteId);

		if (dto.getTitulo() == null || dto.getTitulo().isBlank()) {
			throw new IllegalArgumentException("titulo é obrigatório");
		}
		if (dto.getConteudo() == null || dto.getConteudo().isBlank()) {
			throw new IllegalArgumentException("conteudo é obrigatório");
		}

		final String assinaturaUrl = ArquivoUploadUtil.salvarBase64(dto.getAssinaturaBase64(), DIRETORIO_ASSINATURAS,
				"png");

		final TermoConsentimento termo = TermoConsentimento.builder().paciente(paciente).titulo(dto.getTitulo())
				.conteudo(dto.getConteudo()).criadoPor(usuarioLogado != null ? usuarioLogado.getNome() : null)
				.assinaturaUrl(assinaturaUrl).build();

		return this.toDto(this.termoConsentimentoRepository.save(termo));
	}

	private Paciente exigirPaciente(final Long pacienteId) {
		final Paciente paciente = this.pacienteRepository.findById(pacienteId)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(paciente.getClinicaId())) {
			throw new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!");
		}
		return paciente;
	}

	private TermoConsentimentoDTO toDto(final TermoConsentimento termo) {
		return TermoConsentimentoDTO.builder().id(termo.getId()).pacienteId(termo.getPaciente().getId())
				.titulo(termo.getTitulo()).conteudo(termo.getConteudo()).assinadoEm(termo.getAssinadoEm())
				.criadoPor(termo.getCriadoPor()).assinaturaUrl(termo.getAssinaturaUrl()).build();
	}
}
