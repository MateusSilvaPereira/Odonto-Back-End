package com.msp.odonto_pro.dto.odontograma;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DenteRegistroDTO {

	private String numero;
	private String status;
	private String observacao;
	private LocalDateTime atualizadoEm;
}
