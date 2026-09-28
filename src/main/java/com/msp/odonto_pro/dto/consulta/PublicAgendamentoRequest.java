package com.msp.odonto_pro.dto.consulta;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Body de {@code POST /public/clinicas/{clinicaId}/agendamentos}. */
@Data
public class PublicAgendamentoRequest {

	@NotBlank(message = "nome é obrigatório")
	private String nome;

	@NotBlank(message = "cpf é obrigatório")
	private String cpf;

	private String telefone;
	private String email;
	private LocalDate dataNascimento;

	@NotNull(message = "dentistaId é obrigatório")
	private Long dentistaId;

	@NotNull(message = "data é obrigatório")
	private LocalDate data;

	@NotNull(message = "horario é obrigatório")
	private LocalTime horario;

	@NotBlank(message = "procedimento é obrigatório")
	private String procedimento;
}
