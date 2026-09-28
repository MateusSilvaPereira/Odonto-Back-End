package com.msp.odonto_pro.dto.clinica;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Body de {@code PUT /clinicas/{id}} e {@code PUT /clinicas/me} (variante JSON, sem logo novo). */
@Data
public class ClinicaUpdateRequest {

	@NotBlank(message = "nome é obrigatório")
	private String nome;

	private String cnpj;

	private String telefone;

	private String endereco;
}
