package com.msp.odonto_pro.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.GONE)
public class LinkExpiradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LinkExpiradoException(String message) {
		super(message);
	}

}
