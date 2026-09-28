package com.msp.odonto_pro.entity.dentista;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Janela de atendimento de um dentista em um dia da semana (0=domingo ... 6=sábado, igual a Date.getDay() do JS). */
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HorarioAtendimento {

	@Column(name = "dia_semana", nullable = false)
	private Integer diaSemana;

	@Column(name = "hora_inicio", nullable = false)
	private LocalTime horaInicio;

	@Column(name = "hora_fim", nullable = false)
	private LocalTime horaFim;
}
