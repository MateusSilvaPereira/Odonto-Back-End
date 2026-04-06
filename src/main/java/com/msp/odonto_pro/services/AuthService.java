package com.msp.odonto_pro.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.entity.Usuario;
import com.msp.odonto_pro.repositorys.UsuarioRepository;

@Service
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Usuario autenticar(String username, String password) {

		Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		if (!passwordEncoder.matches(password, usuario.getPassword())) {
			throw new RuntimeException("Senha inválida");
		}

		if (!usuario.isAtivo()) {
			throw new RuntimeException("Usuário inativo");
		}

		return usuario;
	}

	// Salvar usuario...
	public Usuario criarUsuario(Usuario usuario) {
		usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
		return usuarioRepository.save(usuario);
	}
}