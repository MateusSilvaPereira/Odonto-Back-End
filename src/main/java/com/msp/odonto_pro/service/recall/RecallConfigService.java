package com.msp.odonto_pro.service.recall;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.recall.RecallConfigDTO;
import com.msp.odonto_pro.entity.recall.RecallConfig;
import com.msp.odonto_pro.repository.recall.RecallConfigRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * Configuração da campanha de recall — uma linha por clínica (ver
 * RecallConfig). Mesmo formato de payload que NotificacaoConfig, mas
 * corretamente isolado por clínica (o legado é um singleton global).
 */
@Service
@RequiredArgsConstructor
public class RecallConfigService {

	private final RecallConfigRepository repository;

	public RecallConfig obter() {
		final Long clinicaId = SecurityUtils.clinicaIdAtual();
		return this.repository.findByClinicaId(clinicaId)
				.orElseGet(() -> this.repository.save(RecallConfig.builder().clinicaId(clinicaId).build()));
	}

	public RecallConfigDTO obterDto() {
		return this.toDto(this.obter());
	}

	public RecallConfigDTO atualizar(final RecallConfigDTO dto) {
		final RecallConfig config = this.obter();
		config.setAtivoWhatsapp(dto.isAtivoWhatsapp());
		config.setAtivoEmail(dto.isAtivoEmail());
		config.setMensagem(dto.getMensagem());
		config.setAssuntoEmail(dto.getAssuntoEmail());
		return this.toDto(this.repository.save(config));
	}

	private RecallConfigDTO toDto(final RecallConfig config) {
		return new RecallConfigDTO(config.isAtivoWhatsapp(), config.isAtivoEmail(), config.getMensagem(),
				config.getAssuntoEmail());
	}
}
