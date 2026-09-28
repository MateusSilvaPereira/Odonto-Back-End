package com.msp.odonto_pro.dto.clinica;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Body de {@code POST /clinicas}: cria a clínica e seu usuário ADMIN inicial num só passo. */
@Data
public class ClinicaCreateRequest {

	@NotBlank(message = "nome é obrigatório")
	private String nome;

	private String cnpj;

	private String telefone;

	private String endereco;

	@NotBlank(message = "adminNome é obrigatório")
	private String adminNome;

	@NotBlank(message = "adminUsername é obrigatório")
	private String adminUsername;

	@NotBlank(message = "adminEmail é obrigatório")
	@Email(message = "adminEmail inválido")
	private String adminEmail;

	@NotBlank(message = "adminPassword é obrigatório")
	private String adminPassword;
}
