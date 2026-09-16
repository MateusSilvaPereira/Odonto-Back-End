package com.msp.odonto_pro.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dtos.auth.RegisterRequest;
import com.msp.odonto_pro.entity.Usuario;
import com.msp.odonto_pro.enums.Role;
import com.msp.odonto_pro.repositorys.UsuarioRepository;

import jakarta.transaction.Transactional;

/**
 * Serviço responsável por operações de autenticação e registro.
 */
@Service
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(final UsuarioRepository usuarioRepository, final PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Autentica um usuário verificando username, senha e status.
	 * 
	 * @param username nome de usuário
	 * @param password senha em texto puro
	 * @return usuário autenticado
	 * @throws RuntimeException se as credenciais forem inválidas
	 */
	public Usuario autenticar(final String username, final String password) {
		final Usuario usuario = this.usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		if (!this.passwordEncoder.matches(password, usuario.getPassword())) {
			throw new RuntimeException("Senha inválida");
		}

		if (!usuario.isAtivo()) {
			throw new RuntimeException("Usuário inativo");
		}

		return usuario;
	}

	/**
	 * Cria um novo usuário no banco com a senha criptografada.
	 * 
	 * @param usuario usuário a ser criado
	 * @return usuário persistido
	 */
	public Usuario criarUsuario(final Usuario usuario) {
		usuario.setPassword(this.passwordEncoder.encode(usuario.getPassword()));
		return this.usuarioRepository.save(usuario);
	}

	/**
	 * Registra um novo usuário no sistema.
	 * 
	 * Validações: - Username deve ser único - Email deve ser único - Se não
	 * informar role, usa RECEPCIONISTA como padrão (mais seguro)
	 * 
	 * @param request dados do registro
	 * @return usuário criado
	 * @throws RuntimeException se username ou email já existirem
	 */
	@Transactional
	public Usuario register(final RegisterRequest request) {
		if (this.usuarioRepository.existsByUsername(request.getUsername())) {
			throw new RuntimeException("Usuário já existe");
		}

		if (this.usuarioRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("E-mail já cadastrado");
		}

		final Usuario user = new Usuario();
		user.setNome(request.getNome());
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPassword(this.passwordEncoder.encode(request.getPassword()));

		// 🔑 Se não informar role, usa RECEPCIONISTA (princípio do menor privilégio)
		final Role role = request.getRole() != null ? request.getRole() : Role.RECEPCIONISTA;
		user.setRole(role);
		user.setAtivo(true);

		return this.usuarioRepository.save(user);
	}
}