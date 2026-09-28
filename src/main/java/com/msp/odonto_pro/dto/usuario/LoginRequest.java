package com.msp.odonto_pro.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
	@NotBlank(message = "username é obrigatório")
	private String username;

	@NotBlank(message = "password é obrigatório")
	private String password;
}