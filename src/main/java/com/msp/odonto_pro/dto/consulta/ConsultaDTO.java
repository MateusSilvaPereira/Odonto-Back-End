package com.msp.odonto_pro.dto.consulta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultaDTO {

	private Long id;

	@NotNull(message = "pacienteId é obrigatório")
	private Long pacienteId;

	private String nomePaciente;

	private String pacienteFotoUrl;

	@NotNull(message = "data é obrigatório")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate data;

	@NotNull(message = "horario é obrigatório")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	private LocalTime horario;

	private String procedimento;

	private Long dentistaId;

	/** Nome do dentista, resolvido via {@code dentistaId} (ou texto legado, ver Consulta.dentistaLegado). Somente leitura. */
	private String dentista;

	/** Somente leitura; null quando não há dentistaId vinculado. */
	private String dentistaFotoUrl;

	private Integer duracaoMinutos;

	private String status;

	private LocalDateTime lembrete1DiaEnviadoEm;

	private LocalDateTime lembrete1HoraEnviadoEm;

	private LocalDateTime lembreteEmail1DiaEnviadoEm;

	private LocalDateTime lembreteEmail1HoraEnviadoEm;
}
