package com.msp.odonto_pro.dto.recall;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecallConfigDTO {

	private boolean ativoWhatsapp;
	private boolean ativoEmail;
	private String mensagem;
	private String assuntoEmail;
}
