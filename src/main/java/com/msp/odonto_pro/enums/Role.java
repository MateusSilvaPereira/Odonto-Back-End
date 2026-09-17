package com.msp.odonto_pro.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Enum que representa os papéis (roles) do sistema.
 * 
 * Cada Role possui um conjunto de Permissões. Isso permite que um ADMIN tenha
 * todas as permissões, enquanto um RECEPCIONISTA tenha apenas permissões
 * limitadas.
 * 
 * O Spring Security usa o prefixo "ROLE_" para identificar roles. Então ADMIN
 * vira "ROLE_ADMIN" automaticamente.
 */
public enum Role {

	/**
	 * Administrador: acesso total ao sistema. Pode gerenciar usuários, pacientes,
	 * dentistas, etc.
	 */
	ADMIN(Collections.unmodifiableSet(new HashSet<>(Arrays.asList(// Pacientes
			Permission.READ_PACIENTE, Permission.WRITE_PACIENTE, Permission.DELETE_PACIENTE, // Dentistas
			Permission.READ_DENTISTA, Permission.WRITE_DENTISTA, Permission.DELETE_DENTISTA, // Tratamentos
			Permission.READ_TRATAMENTO, Permission.WRITE_TRATAMENTO, Permission.DELETE_TRATAMENTO, // Consultas
			Permission.READ_CONSULTA, Permission.WRITE_CONSULTA, Permission.DELETE_CONSULTA, // Usuários
			Permission.READ_USUARIO, Permission.WRITE_USUARIO, Permission.DELETE_USUARIO)))),

	/**
	 * Dentista: profissional de saúde. Pode ver e gerenciar pacientes e
	 * tratamentos, mas não pode gerenciar outros usuários.
	 */
	DENTISTA(Collections.unmodifiableSet(new HashSet<>(Arrays.asList(// Pacientes
			Permission.READ_PACIENTE, Permission.WRITE_PACIENTE, // Dentistas (pode ver os outros dentistas)
			Permission.READ_DENTISTA, // Tratamentos
			Permission.READ_TRATAMENTO, Permission.WRITE_TRATAMENTO, // Consultas
			Permission.READ_CONSULTA, Permission.WRITE_CONSULTA)))),

	/**
	 * Recepcionista: atendimento inicial. Pode cadastrar pacientes e agendar
	 * consultas, mas não pode deletar registros.
	 */
	RECEPCIONISTA(Collections.unmodifiableSet(new HashSet<>(Arrays.asList(// Pacientes
			Permission.READ_PACIENTE, Permission.WRITE_PACIENTE, // Dentistas (apenas leitura para agendamento)
			Permission.READ_DENTISTA, // Tratamentos (apenas leitura)
			Permission.READ_TRATAMENTO, // Consultas
			Permission.READ_CONSULTA, Permission.WRITE_CONSULTA))));

	/**
	 * Conjunto imutável de permissões associadas a este Role. Usamos
	 * unmodifiableSet para garantir que não seja alterado.
	 */
	private final Set<Permission> permissions;

	/**
	 * Construtor do enum.
	 * 
	 * @param permissions conjunto de permissões do Role
	 */
	Role(final Set<Permission> permissions) {
		this.permissions = permissions;
	}

	/**
	 * Retorna o conjunto de permissões do Role.
	 * 
	 * @return conjunto imutável de permissões
	 */
	public Set<Permission> getPermissions() {
		return this.permissions;
	}

	/**
	 * Converte as permissões do Role em uma lista de GrantedAuthority.
	 * 
	 * O Spring Security usa GrantedAuthority para verificar permissões. Aqui
	 * adicionamos: 1. Todas as permissões granulares (ex: "read:paciente") 2. O
	 * próprio Role com prefixo "ROLE_" (ex: "ROLE_ADMIN")
	 * 
	 * Isso permite usar tanto @PreAuthorize("hasAuthority('read:paciente')")
	 * quanto @PreAuthorize("hasRole('ADMIN')") nos controllers.
	 * 
	 * @return lista de autoridades do Spring Security
	 */
	public List<SimpleGrantedAuthority> getAuthorities() {
		// Converte cada Permission em SimpleGrantedAuthority
		final List<SimpleGrantedAuthority> authorities = this.permissions.stream()
				.map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());

		// Adiciona o próprio Role como autoridade (com prefixo ROLE_)
		authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

		return authorities;
	}
}