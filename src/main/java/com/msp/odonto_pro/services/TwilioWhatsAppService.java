package com.msp.odonto_pro.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

/**
 * Envio de mensagens de WhatsApp via Twilio.
 *
 * Requer as variáveis de ambiente TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN e
 * TWILIO_WHATSAPP_FROM (ex: "whatsapp:+14155238886" no sandbox, ou o número
 * aprovado em produção). Se não estiverem configuradas, o serviço fica
 * inativo e apenas loga um aviso ao tentar enviar, sem quebrar a aplicação.
 */
@Service
public class TwilioWhatsAppService implements WhatsAppService {

	private static final Logger log = LoggerFactory.getLogger(TwilioWhatsAppService.class);

	@Value("${twilio.account-sid:}")
	private String accountSid;

	@Value("${twilio.auth-token:}")
	private String authToken;

	@Value("${twilio.whatsapp-from:}")
	private String whatsappFrom;

	private boolean configurado;

	@PostConstruct
	void init() {
		this.configurado = this.naoVazio(this.accountSid) && this.naoVazio(this.authToken)
				&& this.naoVazio(this.whatsappFrom);

		if (this.configurado) {
			Twilio.init(this.accountSid, this.authToken);
			log.info("Integração Twilio WhatsApp inicializada.");
		} else {
			log.warn("Twilio não configurado (TWILIO_ACCOUNT_SID / TWILIO_AUTH_TOKEN / TWILIO_WHATSAPP_FROM "
					+ "ausentes). Lembretes automáticos por WhatsApp ficarão desativados até configurar.");
		}
	}

	@Override
	public void enviarMensagem(final String telefoneE164, final String mensagem) {
		if (!this.configurado) {
			// Lança exceção (em vez de só logar) para que o chamador NÃO marque o
			// lembrete como enviado — senão, uma vez configuradas as credenciais,
			// esse lembrete nunca mais seria reenviado.
			throw new IllegalStateException("Twilio não configurado (TWILIO_ACCOUNT_SID / TWILIO_AUTH_TOKEN / "
					+ "TWILIO_WHATSAPP_FROM ausentes). Destinatário não notificado: " + telefoneE164);
		}

		final Message mensagemCriada = Message
				.creator(new PhoneNumber("whatsapp:" + telefoneE164), new PhoneNumber(this.whatsappFrom), mensagem)
				.create();

		log.info("Mensagem WhatsApp enviada via Twilio. sid={} status={} destinatario={}", mensagemCriada.getSid(),
				mensagemCriada.getStatus(), telefoneE164);
	}

	private boolean naoVazio(final String valor) {
		return valor != null && !valor.isBlank();
	}
}
