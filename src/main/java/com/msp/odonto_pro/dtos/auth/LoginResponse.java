package com.msp.odonto_pro.dtos.auth;

import com.msp.odonto_pro.entity.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
	private String token;
	private UserDTO user;

	public LoginResponse(String token, Usuario usuario) {
		this.token = token;
		this.user = new UserDTO(usuario.getId(), usuario.getNome(), usuario.getUsername(), usuario.getEmail(),
				usuario.getTelefone(), usuario.getAvatar());
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserDTO {
		private Long id;
		private String nome;
		private String username;
		private String email;
		private String telefone;
		private String avatar;
	}
}