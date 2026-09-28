package com.msp.odonto_pro.service.shared;

/**
 * Abstração de envio de e-mail. Implementação atual usa SMTP (ver
 * SmtpEmailService), mas o resto da aplicação não depende disso diretamente.
 */
public interface EmailService {

	/**
	 * Envia um e-mail.
	 *
	 * @param destinatario endereço de destino
	 * @param assunto      assunto do e-mail
	 * @param corpoHtml    corpo em HTML
	 */
	void enviar(String destinatario, String assunto, String corpoHtml);
}
