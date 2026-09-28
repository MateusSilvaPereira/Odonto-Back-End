package com.msp.odonto_pro.dto.notificacao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacaoConfigDTO {

	private boolean ativoWhatsapp;
	private boolean ativoEmail;
	private String mensagem1Dia;
	private String mensagem1Hora;
	private String assuntoEmail1Dia;
	private String assuntoEmail1Hora;
}
