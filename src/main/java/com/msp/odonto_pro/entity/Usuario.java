package com.msp.odonto_pro.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

	@Column(nullable = false)
	private String role;

	@Column(nullable = false)
	private boolean ativo = true;

	private LocalDateTime criadoEm;

	private LocalDateTime atualizadoEm;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
		this.atualizadoEm = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.atualizadoEm = LocalDateTime.now();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptyList(); 
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
	public boolean isAccountNonExpired() { return true; }

	@Override
	public boolean isAccountNonLocked() { return true; }

	@Override
	public boolean isCredentialsNonExpired() { return true; }

	@Override
	public boolean isEnabled() {
		return this.ativo; 
	}


	public LocalDateTime getCriadoEm() { return criadoEm; }
	public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}