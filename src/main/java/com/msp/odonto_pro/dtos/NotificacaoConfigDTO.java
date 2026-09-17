package com.msp.odonto_pro.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacaoConfigDTO {

	private boolean ativo;
	private String mensagem1Dia;
	private String mensagem1Hora;
}
