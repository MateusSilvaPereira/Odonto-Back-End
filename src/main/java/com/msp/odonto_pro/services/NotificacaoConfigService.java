package com.msp.odonto_pro.services;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dtos.NotificacaoConfigDTO;
import com.msp.odonto_pro.entity.NotificacaoConfig;
import com.msp.odonto_pro.repositorys.NotificacaoConfigRepository;

import lombok.RequiredArgsConstructor;

/**
 * Configuração única e global (não por usuário) dos lembretes automáticos.
 */
@Service
@RequiredArgsConstructor
public class NotificacaoConfigService {

	private static final Long ID_CONFIG = 1L;

	private static final String MENSAGEM_1_DIA_PADRAO = "Olá {nome}! Lembrando que você tem uma consulta amanhã, "
			+ "dia {data} às {hora}, para {procedimento} com {dentista}. Até lá!";

	private static final String MENSAGEM_1_HORA_PADRAO = "Olá {nome}! Sua consulta é daqui a 1 hora, às {hora}, "
			+ "para {procedimento} com {dentista}. Te esperamos!";

	private final NotificacaoConfigRepository repository;

	public NotificacaoConfig obter() {
		return this.repository.findById(ID_CONFIG).orElseGet(this::criarConfigPadrao);
	}

	public NotificacaoConfigDTO obterDto() {
		return this.toDto(this.obter());
	}

	public NotificacaoConfigDTO atualizar(final NotificacaoConfigDTO dto) {
		final NotificacaoConfig config = this.obter();
		config.setAtivo(dto.isAtivo());
		config.setMensagem1Dia(dto.getMensagem1Dia());
		config.setMensagem1Hora(dto.getMensagem1Hora());
		return this.toDto(this.repository.save(config));
	}

	private NotificacaoConfig criarConfigPadrao() {
		final NotificacaoConfig config = new NotificacaoConfig();
		config.setId(ID_CONFIG);
		config.setAtivo(true);
		config.setMensagem1Dia(MENSAGEM_1_DIA_PADRAO);
		config.setMensagem1Hora(MENSAGEM_1_HORA_PADRAO);
		return this.repository.save(config);
	}

	private NotificacaoConfigDTO toDto(final NotificacaoConfig config) {
		return new NotificacaoConfigDTO(config.isAtivo(), config.getMensagem1Dia(), config.getMensagem1Hora());
	}
}
