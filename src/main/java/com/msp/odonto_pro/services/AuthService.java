package com.msp.odonto_pro.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dtos.auth.RegisterRequest;
import com.msp.odonto_pro.entity.Usuario;
import com.msp.odonto_pro.repositorys.UsuarioRepository;

import jakarta.transaction.Transactional;

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

	public Usuario criarUsuario(Usuario usuario) {
		usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
		return usuarioRepository.save(usuario);
	}

	@Transactional
	public Usuario register(RegisterRequest request) {
		if (usuarioRepository.existsByUsername(request.getUsername())) {
			throw new RuntimeException("Usuário já existe");
		}
		if (usuarioRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("E-mail já cadastrado");
		}

		Usuario user = new Usuario();
		user.setNome(request.getNome());
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole("USER");
		user.setAtivo(true);

		return usuarioRepository.save(user);
	}
}