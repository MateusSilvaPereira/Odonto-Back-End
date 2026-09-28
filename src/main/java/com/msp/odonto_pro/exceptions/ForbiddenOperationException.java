package com.msp.odonto_pro.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Operação recusada por regra de autorização de negócio (ex: mexer na
 * própria conta, promover a um papel restrito). Mapeada para 403.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenOperationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ForbiddenOperationException(final String message) {
		super(message);
	}

}
