package com.msp.odonto_pro.security;

import org.springframework.security.core.context.SecurityContextHolder;

import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.enums.Role;

/**
 * Acesso ao usuário autenticado (via SecurityContext) a partir de qualquer
 * service/controller, sem precisar receber {@code Authentication} como
 * parâmetro em toda assinatura.
 *
 * Usado principalmente para derivar o {@code clinicaId} do isolamento
 * multi-clínica a partir do token, nunca do payload enviado pelo front.
 */
public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static Usuario usuarioLogado() {
		final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!(principal instanceof Usuario)) {
			throw new IllegalStateException("Usuário autenticado não encontrado no contexto de segurança");
		}
		return (Usuario) principal;
	}

	/**
	 * Id da clínica do usuário autenticado.
	 *
	 * @throws IllegalStateException se o usuário for SUPER_ADMIN (não possui
	 *                                clínica) ou não estiver vinculado a nenhuma
	 */
	public static Long clinicaIdAtual() {
		final Usuario usuario = usuarioLogado();
		if (usuario.getClinicaId() == null) {
			throw new IllegalStateException("Usuário autenticado não está vinculado a nenhuma clínica");
		}
		return usuario.getClinicaId();
	}

	public static boolean isSuperAdmin() {
		return usuarioLogado().getRole() == Role.SUPER_ADMIN;
	}
}
