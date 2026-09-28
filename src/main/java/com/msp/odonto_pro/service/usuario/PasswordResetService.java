package com.msp.odonto_pro.service.usuario;
import com.msp.odonto_pro.service.shared.EmailService;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.entity.usuario.PasswordResetToken;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.enums.Role;
import com.msp.odonto_pro.exceptions.LinkExpiradoException;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.usuario.PasswordResetTokenRepository;
import com.msp.odonto_pro.repository.usuario.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Fluxo de "esqueci minha senha" / troca de senha.
 *
 * {@link #solicitarRedefinicao(String)} nunca revela se o e-mail existe (só
 * loga/envia quando existe) — a resposta genérica de sucesso é dada sempre
 * pelo controller, independentemente do resultado aqui.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private static final int SENHA_MIN_LENGTH = 6;

	private final UsuarioRepository usuarioRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;

	@Value("${app.frontend-url}")
	private String frontendUrl;

	@Value("${app.password-reset.expiracao-minutos:60}")
	private long expiracaoMinutos;

	@Transactional
	public void solicitarRedefinicao(final String email) {
		this.usuarioRepository.findByEmail(email).filter(Usuario::isAtivo)
				.ifPresent(this::gerarEEnviarToken);
	}

	/** Disparado por um ADMIN/SUPER_ADMIN para outro usuário (ver UsuarioController). */
	@Transactional
	public void enviarRedefinicaoParaUsuario(final Long usuarioAlvoId, final Usuario usuarioAutenticado) {
		final Usuario alvo = this.usuarioRepository.findById(usuarioAlvoId)
				.orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

		final boolean mesmaClinica = usuarioAutenticado.getRole() == Role.SUPER_ADMIN
				|| usuarioAutenticado.getClinicaId() != null && usuarioAutenticado.getClinicaId().equals(alvo.getClinicaId());
		if (!mesmaClinica) {
			throw new NotFoundException("Usuário não encontrado");
		}

		this.gerarEEnviarToken(alvo);
	}

	@Transactional
	public void redefinirSenha(final String token, final String novaSenha) {
		this.validarSenha(novaSenha);

		final PasswordResetToken resetToken = this.passwordResetTokenRepository.findByToken(token)
				.orElseThrow(() -> new LinkExpiradoException("Link de redefinição inválido ou já utilizado"));

		if (!resetToken.isValido()) {
			throw new LinkExpiradoException("Link de redefinição expirado ou já utilizado");
		}

		final Usuario usuario = this.usuarioRepository.findById(resetToken.getUsuarioId())
				.orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

		usuario.setPassword(this.passwordEncoder.encode(novaSenha));
		this.usuarioRepository.save(usuario);

		resetToken.setUsadoEm(LocalDateTime.now());
		this.passwordResetTokenRepository.save(resetToken);
	}

	public void alterarSenha(final Usuario usuarioLogado, final String senhaAtual, final String novaSenha) {
		if (!this.passwordEncoder.matches(senhaAtual, usuarioLogado.getPassword())) {
			throw new IllegalArgumentException("Senha atual incorreta");
		}
		this.validarSenha(novaSenha);

		usuarioLogado.setPassword(this.passwordEncoder.encode(novaSenha));
		this.usuarioRepository.save(usuarioLogado);
	}

	private void gerarEEnviarToken(final Usuario usuario) {
		this.passwordResetTokenRepository.deleteByUsuarioId(usuario.getId());

		final PasswordResetToken resetToken = PasswordResetToken.builder().usuarioId(usuario.getId())
				.token(UUID.randomUUID().toString()).expiraEm(LocalDateTime.now().plusMinutes(this.expiracaoMinutos))
				.build();
		this.passwordResetTokenRepository.save(resetToken);

		final String link = this.frontendUrl + "/redefinir-senha/" + resetToken.getToken();
		final String corpo = "<p>Olá, " + usuario.getNome() + ".</p>"
				+ "<p>Recebemos uma solicitação para redefinir sua senha no Odonto Pro. Clique no link abaixo "
				+ "para escolher uma nova senha (válido por " + this.expiracaoMinutos + " minutos):</p>"
				+ "<p><a href=\"" + link + "\">" + link + "</a></p>"
				+ "<p>Se você não solicitou isso, ignore este e-mail.</p>";

		try {
			this.emailService.enviar(usuario.getEmail(), "Redefinição de senha - Odonto Pro", corpo);
		} catch (final Exception e) {
			// Já logado dentro do EmailService. Nunca deixa a falha de envio (ex:
			// SMTP não configurado) vazar pro chamador — a resposta pro cliente
			// tem que continuar sendo sempre o sucesso genérico (ver
			// AuthController.esqueciSenha).
		}
	}

	private void validarSenha(final String senha) {
		if (senha == null || senha.length() < SENHA_MIN_LENGTH) {
			throw new IllegalArgumentException("A senha deve ter ao menos " + SENHA_MIN_LENGTH + " caracteres");
		}
	}
}
