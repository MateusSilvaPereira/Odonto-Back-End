package com.msp.odonto_pro.dto.consulta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dados mínimos de um dentista expostos publicamente (widget de agendamento online, sem autenticação). */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicDentistaDTO {

	private Long id;
	private String nome;
	private String corAgenda;
}
