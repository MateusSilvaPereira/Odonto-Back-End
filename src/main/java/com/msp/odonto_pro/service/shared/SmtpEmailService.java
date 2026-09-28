package com.msp.odonto_pro.service.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

/**
 * Envio de e-mail via SMTP.
 *
 * Requer a variável de ambiente SMTP_HOST (+ SMTP_PORT/SMTP_USERNAME/
 * SMTP_PASSWORD conforme o provedor). Se não estiver configurada, ou se o
 * envio falhar, o conteúdo é logado e uma exceção é lançada (em vez de só
 * engolir o erro) — mesmo motivo do TwilioWhatsAppService: um chamador que
 * marca "lembrete enviado" a partir do retorno normal (ver
 * LembreteConsultaScheduler) não pode marcar como enviado algo que não foi.
 * Quem quiser degradar graciosamente (ex: PasswordResetService, que sempre
 * responde sucesso genérico) deve capturar a exceção no próprio chamador.
 */
@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

	private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

	private final JavaMailSender javaMailSender;

	@Value("${spring.mail.host:}")
	private String smtpHost;

	@Value("${app.mail.from}")
	private String remetente;

	@Override
	public void enviar(final String destinatario, final String assunto, final String corpoHtml) {
		if (this.smtpHost == null || this.smtpHost.isBlank()) {
			log.warn("SMTP não configurado (SMTP_HOST ausente). E-mail NÃO enviado — conteúdo abaixo, "
					+ "só para dev/homologação:\npara={}\nassunto={}\ncorpo:\n{}", destinatario, assunto, corpoHtml);
			throw new IllegalStateException(
					"SMTP não configurado (SMTP_HOST ausente). Destinatário não notificado: " + destinatario);
		}

		try {
			final MimeMessage mensagem = this.javaMailSender.createMimeMessage();
			final MimeMessageHelper helper = new MimeMessageHelper(mensagem, "UTF-8");
			helper.setFrom(this.remetente);
			helper.setTo(destinatario);
			helper.setSubject(assunto);
			helper.setText(corpoHtml, true);
			this.javaMailSender.send(mensagem);
			log.info("E-mail enviado. destinatario={} assunto={}", destinatario, assunto);
		} catch (final Exception e) {
			log.error("Falha ao enviar e-mail. destinatario={} assunto={}", destinatario, assunto, e);
			throw new RuntimeException("Falha ao enviar e-mail para " + destinatario, e);
		}
	}
}
