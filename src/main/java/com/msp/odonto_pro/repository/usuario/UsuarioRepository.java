package com.msp.odonto_pro.repository.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msp.odonto_pro.entity.usuario.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByUsername(String username);

	Optional<Usuario> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	long countByAtivoTrue();

	long countByAtivoTrueAndClinicaId(Long clinicaId);

}

