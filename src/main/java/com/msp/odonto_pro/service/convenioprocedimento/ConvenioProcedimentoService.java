package com.msp.odonto_pro.service.convenioprocedimento;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.convenioprocedimento.ConvenioProcedimentoDTO;
import com.msp.odonto_pro.entity.convenioprocedimento.ConvenioProcedimento;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.convenioprocedimento.ConvenioProcedimentoRepository;
import com.msp.odonto_pro.repository.convenio.ConvenioRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/** Tabela de procedimentos/valores de um convênio (ver Convenio/ConvenioController). */
@Service
@RequiredArgsConstructor
public class ConvenioProcedimentoService {

	private final ConvenioProcedimentoRepository convenioProcedimentoRepository;
	private final ConvenioRepository convenioRepository;

	public List<ConvenioProcedimentoDTO> listar(final Long convenioId) {
		this.exigirConvenio(convenioId);
		return this.convenioProcedimentoRepository.findByConvenioId(convenioId).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	public ConvenioProcedimentoDTO criar(final Long convenioId, final ConvenioProcedimentoDTO dto) {
		this.exigirConvenio(convenioId);
		this.validar(dto);

		final ConvenioProcedimento procedimento = ConvenioProcedimento.builder().convenioId(convenioId)
				.procedimento(dto.getProcedimento().trim()).valor(dto.getValor()).build();

		return this.toDto(this.convenioProcedimentoRepository.save(procedimento));
	}

	public ConvenioProcedimentoDTO atualizar(final Long convenioId, final Long id, final ConvenioProcedimentoDTO dto) {
		this.validar(dto);
		final ConvenioProcedimento procedimento = this.exigir(convenioId, id);
		procedimento.setProcedimento(dto.getProcedimento().trim());
		procedimento.setValor(dto.getValor());
		return this.toDto(this.convenioProcedimentoRepository.save(procedimento));
	}

	public void excluir(final Long convenioId, final Long id) {
		this.exigir(convenioId, id);
		this.convenioProcedimentoRepository.deleteById(id);
	}

	private void exigirConvenio(final Long convenioId) {
		if (!this.convenioRepository.existsByIdAndClinicaId(convenioId, SecurityUtils.clinicaIdAtual())) {
			throw new NotFoundException("Convênio com id: " + convenioId + " não encontrado!");
		}
	}

	private ConvenioProcedimento exigir(final Long convenioId, final Long id) {
		if (!this.convenioProcedimentoRepository.existsByIdAndConvenioId(id, convenioId)) {
			throw new NotFoundException("Procedimento com id: " + id + " não encontrado para este convênio!");
		}
		return this.convenioProcedimentoRepository.findById(id).orElseThrow();
	}

	private void validar(final ConvenioProcedimentoDTO dto) {
		if (dto.getProcedimento() == null || dto.getProcedimento().isBlank()) {
			throw new IllegalArgumentException("procedimento é obrigatório");
		}
		if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("valor é obrigatório e deve ser maior ou igual a zero");
		}
	}

	private ConvenioProcedimentoDTO toDto(final ConvenioProcedimento procedimento) {
		return ConvenioProcedimentoDTO.builder().id(procedimento.getId()).convenioId(procedimento.getConvenioId())
				.procedimento(procedimento.getProcedimento()).valor(procedimento.getValor()).build();
	}
}
