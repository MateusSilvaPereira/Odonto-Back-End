package com.msp.odonto_pro.service.usuario;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.usuario.LoginRequest;
import com.msp.odonto_pro.dto.usuario.LoginResponse;
import com.msp.odonto_pro.dto.usuario.RegisterRequest;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.enums.Role;
import com.msp.odonto_pro.exceptions.ConflitoException;
import com.msp.odonto_pro.repository.clinica.ClinicaRepository;
import com.msp.odonto_pro.repository.usuario.UsuarioRepository;
import com.msp.odonto_pro.security.JwtService;
import com.msp.odonto_pro.security.UserDetailsServiceImp;

import jakarta.transaction.Transactional;

/**
 * Serviço responsável por operações de autenticação e registro.
 */
@Service
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final ClinicaRepository clinicaRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final UserDetailsServiceImp userDetailsService;
	private final JwtService jwtService;

	public AuthService(final UsuarioRepository usuarioRepository, final ClinicaRepository clinicaRepository,
			final PasswordEncoder passwordEncoder, final AuthenticationManager authenticationManager,
			final UserDetailsServiceImp userDetailsService, final JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.clinicaRepository = clinicaRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.jwtService = jwtService;
	}

	/**
	 * Autentica as credenciais, gera o token JWT e monta a resposta com os
	 * dados do usuário (incluindo o nome da clínica, se houver).
	 */
	public LoginResponse login(final LoginRequest request) {
		final Authentication authentication = this.authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getUsername());
		final String token = this.jwtService.generateToken(userDetails);

		final Usuario usuario = (Usuario) userDetails;
		final String clinicaNome = usuario.getClinicaId() != null
				? this.clinicaRepository.findById(usuario.getClinicaId()).map(c -> c.getNome()).orElse(null)
				: null;
		return new LoginResponse(token, usuario, clinicaNome);
	}

	/**
	 * Registra um novo usuário no sistema. Endpoint público (sem autenticação —
	 * ver SecurityConfig), então NUNCA confia no {@code role} enviado pelo
	 * cliente: sempre RECEPCIONISTA, independentemente do que vier no payload.
	 * Sem isso, qualquer chamador anônimo poderia se auto-registrar como
	 * ADMIN ou até SUPER_ADMIN.
	 *
	 * Usuários criados aqui não pertencem a nenhuma clínica (multi-clínica: só
	 * SUPER_ADMIN, via /clinicas, ou um ADMIN já autenticado, via
	 * /usuarios, podem vincular alguém a uma clínica) — na prática ficam
	 * sem acesso a nada até serem migrados manualmente.
	 *
	 * @throws ConflitoException se username ou email já existirem
	 */
	@Transactional
	public Usuario register(final RegisterRequest request) {
		if (this.usuarioRepository.existsByUsername(request.getUsername())) {
			throw new ConflitoException("Usuário já existe");
		}

		if (this.usuarioRepository.existsByEmail(request.getEmail())) {
			throw new ConflitoException("E-mail já cadastrado");
		}

		final Usuario user = new Usuario();
		user.setNome(request.getNome());
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPassword(this.passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.RECEPCIONISTA);
		user.setAtivo(true);

		return this.usuarioRepository.save(user);
	}
}
