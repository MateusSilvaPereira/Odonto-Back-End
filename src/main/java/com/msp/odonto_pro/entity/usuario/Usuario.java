package com.msp.odonto_pro.entity.usuario;

import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.msp.odonto_pro.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um usuário do sistema.
 * 
 * Implementa UserDetails do Spring Security para ser usado no processo de
 * autenticação e autorização.
 * 
 * IMPORTANTE: O campo 'role' foi migrado de String para Enum Role. No banco de
 * dados, o valor será salvo como String (ex: "ADMIN"), mas no Java trabalhamos
 * com o enum Role (type-safe).
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuarios")
public class Usuario implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(nullable = false, unique = true, length = 120)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String nome;

	@Column(length = 20)
	private String telefone;

	@Column
	private String avatar;

	/**
	 * Papel do usuário no sistema.
	 * 
	 * @Enumerated(EnumType.STRING) faz com que o valor seja salvo como String no
	 *                              banco (ex: "ADMIN", "DENTISTA").
	 * 
	 *                              Se usássemos EnumType.ORDINAL, seria salvo como
	 *                              número (0, 1, 2), o que é perigoso pois
	 *                              reordenar os enums quebraria o banco.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	/**
	 * Clínica (tenant) à qual este usuário pertence. Obrigatória para
	 * ADMIN/DENTISTA/RECEPCIONISTA; nula para SUPER_ADMIN, que não pertence a
	 * nenhuma clínica específica. Não é FK JPA (@ManyToOne) de propósito: mantém
	 * o carregamento do usuário leve (usado a cada requisição autenticada) e
	 * evita join implícito nas telas de login.
	 */
	@Column(name = "clinica_id")
	private Long clinicaId;

	@Column(nullable = false)
	private boolean ativo = true;

	private LocalDateTime criadoEm;

	private LocalDateTime atualizadoEm;

	/**
	 * Callback executado antes de persistir o usuário pela primeira vez. Define as
	 * datas de criação e atualização.
	 */
	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
		this.atualizadoEm = LocalDateTime.now();
	}

	/**
	 * Callback executado antes de atualizar o usuário. Atualiza a data de
	 * modificação.
	 */
	@PreUpdate
	public void preUpdate() {
		this.atualizadoEm = LocalDateTime.now();
	}

	// Delega para o Role, que já monta as permissões granulares + o próprio Role com prefixo "ROLE_".
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.role.getAuthorities();
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.ativo;
	}
}