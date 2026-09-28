package com.msp.odonto_pro.dto.cobranca;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CobrancaOnlineDTO {

	private Long id;

	private Long itemTratamentoId;

	private Double valor;

	private String forma;

	private String status;

	private String qrCode;

	private String linhaDigitavel;

	private LocalDate vencimento;

	private LocalDateTime criadoEm;

	private LocalDateTime pagoEm;
}
