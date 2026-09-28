package com.msp.odonto_pro.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Limite de tentativas excedido (ver PublicBookingRateLimiter). Mapeada para 429. */
@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TooManyRequestsException(final String message) {
		super(message);
	}

}
