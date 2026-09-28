package com.msp.odonto_pro.dto.cobranca;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** Body de {@code POST /cobrancas}. */
@Data
public class CobrancaCreateRequest {

	@NotNull(message = "itemTratamentoId é obrigatório")
	private Long itemTratamentoId;

	@NotNull(message = "valor é obrigatório")
	@Positive(message = "valor deve ser maior que zero")
	private Double valor;

	/** "pix" ou "boleto". */
	@NotBlank(message = "forma é obrigatório")
	private String forma;

	/** Opcional, só relevante para forma=boleto. Sem fallback no backend — ver CobrancaOnline.vencimento. */
	private LocalDate vencimento;
}
