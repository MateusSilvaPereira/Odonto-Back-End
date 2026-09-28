package com.msp.odonto_pro.repository.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.usuario.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	Optional<PasswordResetToken> findByToken(String token);

	@Modifying
	@Query("DELETE FROM PasswordResetToken t WHERE t.usuarioId = :usuarioId")
	void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);

}
