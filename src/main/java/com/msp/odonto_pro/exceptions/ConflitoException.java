package com.msp.odonto_pro.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Regra de negócio violada por um vínculo existente (ex: excluir um
 * dentista que ainda tem consultas atreladas a ele). Mapeada para 409.
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConflitoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConflitoException(final String message) {
		super(message);
	}

}
