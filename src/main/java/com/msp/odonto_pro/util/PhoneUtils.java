package com.msp.odonto_pro.util;

/**
 * Normalização de telefones brasileiros para o formato E.164 (+55DDDNÚMERO),
 * exigido pela API do WhatsApp/Twilio. Os telefones são salvos no banco com
 * formatação livre (parênteses, traços, etc.), então aqui só ficam os dígitos.
 */
public final class PhoneUtils {

	private PhoneUtils() {
	}

	public static String paraE164Brasil(final String telefone) {
		if (telefone == null) {
			return null;
		}

		String digitos = telefone.replaceAll("\\D", "");
		if (digitos.isEmpty()) {
			return null;
		}

		if (!digitos.startsWith("55")) {
			digitos = "55" + digitos;
		}

		return "+" + digitos;
	}
}
