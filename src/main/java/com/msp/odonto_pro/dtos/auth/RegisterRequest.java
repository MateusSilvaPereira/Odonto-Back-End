package com.msp.odonto_pro.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
	private String nome;
	private String username;
	private String email;
	private String password;
}