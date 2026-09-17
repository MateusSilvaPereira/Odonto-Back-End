package com.msp.odonto_pro.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_CONSULTA)
public class Consulta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id", nullable = false)
	private Paciente paciente;

	@Column(nullable = false)
	private LocalDate data;

	@Column(name = "hora", nullable = false)
	private LocalTime horario;

	private String procedimento;

	/**
	 * Nome do dentista em texto livre. Ainda não há cadastro próprio de
	 * dentistas vinculado à consulta.
	 */
	private String dentista;

	@Column(nullable = false)
	private String status;

	/**
	 * Timestamp de quando o lembrete de 1 dia antes foi enviado. Null =
	 * ainda não enviado. Usado para garantir idempotência por consulta.
	 */
	private LocalDateTime lembrete1DiaEnviadoEm;

	/**
	 * Timestamp de quando o lembrete de 1 hora antes foi enviado. Null =
	 * ainda não enviado.
	 */
	private LocalDateTime lembrete1HoraEnviadoEm;
}
