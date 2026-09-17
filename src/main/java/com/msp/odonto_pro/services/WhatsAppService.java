package com.msp.odonto_pro.services;

/**
 * Abstração de envio de mensagens de WhatsApp. Implementação atual usa
 * Twilio, mas o resto da aplicação (scheduler de lembretes) não depende
 * disso diretamente.
 */
public interface WhatsAppService {

	/**
	 * Envia uma mensagem de WhatsApp.
	 *
	 * @param telefoneE164 telefone de destino no formato +55DDDNÚMERO
	 * @param mensagem     texto já com os placeholders resolvidos
	 */
	void enviarMensagem(String telefoneE164, String mensagem);
}
