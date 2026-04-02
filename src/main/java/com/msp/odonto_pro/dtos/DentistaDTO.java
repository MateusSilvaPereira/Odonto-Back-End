package com.msp.odonto_pro.dtos;

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
public class DentistaDTO {

	private Long id;

	private String nome;

	private String cpf;

	private String cro;

	private String telefone;

	private String email;

	private String especialidade;

	private Boolean ativo;

	private List<Consulta> consultas;
}
