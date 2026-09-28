package com.msp.odonto_pro.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsqueciSenhaRequest {
	@NotBlank(message = "email é obrigatório")
	@Email(message = "email inválido")
	private String email;
}
