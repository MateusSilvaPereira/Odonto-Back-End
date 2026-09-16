package com.msp.odonto_pro.dtos.auth;

import com.msp.odonto_pro.enums.Role;

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

	private String nome;
	private String username;
	private String email;
	private String password;

	/**
	 * Papel do usuário (opcional). Se null, será atribuído RECEPCIONISTA.
	 */
	private Role role;
}