package com.msp.odonto_pro.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Envelope de paginação no formato esperado pelo front-end: content,
 * totalElements, totalPages e number (página atual, 0-based).
 *
 * Usado ao invés de devolver Page&lt;T&gt; diretamente para não depender do
 * formato de serialização JSON do Spring Data, que pode mudar entre versões.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {

	private List<T> content;
	private long totalElements;
	private int totalPages;
	private int number;
}
