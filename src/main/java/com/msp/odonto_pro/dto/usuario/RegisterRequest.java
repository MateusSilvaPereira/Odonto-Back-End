package com.msp.odonto_pro.dto.usuario;

import com.msp.odonto_pro.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de registro de novo usuário.
 *
 * O campo 'role' é opcional. Se não informado, o sistema atribui RECEPCIONISTA
 * como padrão (mais seguro).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	@NotBlank(message = "nome é obrigatório")
	private String nome;

	@NotBlank(message = "username é obrigatório")
	private String username;

	@NotBlank(message = "email é obrigatório")
	@Email(message = "email inválido")
	private String email;

	@NotBlank(message = "password é obrigatório")
	@Size(min = 6, message = "password deve ter ao menos 6 caracteres")
	private String password;

	/**
	 * Papel do usuário (opcional). Se null, será atribuído RECEPCIONISTA.
	 */
	private Role role;
}