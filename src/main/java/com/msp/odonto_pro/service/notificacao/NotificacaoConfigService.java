package com.msp.odonto_pro.service.notificacao;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.notificacao.NotificacaoConfigDTO;
import com.msp.odonto_pro.entity.notificacao.NotificacaoConfig;
import com.msp.odonto_pro.repository.notificacao.NotificacaoConfigRepository;

import lombok.RequiredArgsConstructor;

/**
 * Configuração única e global (não por usuário) dos lembretes automáticos.
 */
@Service
@RequiredArgsConstructor
public class NotificacaoConfigService {

	private static final Long ID_CONFIG = 1L;

	private static final String MENSAGEM_1_DIA_PADRAO = "Olá {nome}! Lembrando que você tem uma consulta amanhã, "
			+ "dia {data} às {hora}, para {procedimento} com {dentista}. Confirme ou cancele aqui: {link}";

	private static final String MENSAGEM_1_HORA_PADRAO = "Olá {nome}! Sua consulta é daqui a 1 hora, às {hora}, "
			+ "para {procedimento} com {dentista}. Confirme ou cancele aqui: {link}";

	private static final String ASSUNTO_EMAIL_1_DIA_PADRAO = "Lembrete: sua consulta é amanhã";
	private static final String ASSUNTO_EMAIL_1_HORA_PADRAO = "Lembrete: sua consulta é daqui a 1 hora";

	private final NotificacaoConfigRepository repository;

	public NotificacaoConfig obter() {
		return this.repository.findById(ID_CONFIG).orElseGet(this::criarConfigPadrao);
	}

	public NotificacaoConfigDTO obterDto() {
		return this.toDto(this.obter());
	}

	public NotificacaoConfigDTO atualizar(final NotificacaoConfigDTO dto) {
		final NotificacaoConfig config = this.obter();
		config.setAtivoWhatsapp(dto.isAtivoWhatsapp());
		config.setAtivoEmail(dto.isAtivoEmail());
		config.setMensagem1Dia(dto.getMensagem1Dia());
		config.setMensagem1Hora(dto.getMensagem1Hora());
		config.setAssuntoEmail1Dia(dto.getAssuntoEmail1Dia());
		config.setAssuntoEmail1Hora(dto.getAssuntoEmail1Hora());
		return this.toDto(this.repository.save(config));
	}

	private NotificacaoConfig criarConfigPadrao() {
		final NotificacaoConfig config = new NotificacaoConfig();
		config.setId(ID_CONFIG);
		config.setAtivoWhatsapp(true);
		config.setAtivoEmail(false);
		config.setMensagem1Dia(MENSAGEM_1_DIA_PADRAO);
		config.setMensagem1Hora(MENSAGEM_1_HORA_PADRAO);
		config.setAssuntoEmail1Dia(ASSUNTO_EMAIL_1_DIA_PADRAO);
		config.setAssuntoEmail1Hora(ASSUNTO_EMAIL_1_HORA_PADRAO);
		return this.repository.save(config);
	}

	private NotificacaoConfigDTO toDto(final NotificacaoConfig config) {
		return new NotificacaoConfigDTO(config.isAtivoWhatsapp(), config.isAtivoEmail(), config.getMensagem1Dia(),
				config.getMensagem1Hora(), config.getAssuntoEmail1Dia(), config.getAssuntoEmail1Hora());
	}
}
