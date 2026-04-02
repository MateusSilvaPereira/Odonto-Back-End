package com.msp.odonto_pro.dtos;

import java.time.LocalDate;
import java.util.List;

import com.msp.odonto_pro.entity.Consulta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PacienteDTO {

	private Long id;

	private String nome;

	private String cpf;

	private String telefone;

	private String email;

	private LocalDate dataNascimento;

	private List<Consulta> consultas;
}
