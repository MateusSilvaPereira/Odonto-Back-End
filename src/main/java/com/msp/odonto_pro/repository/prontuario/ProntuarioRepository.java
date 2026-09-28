package com.msp.odonto_pro.repository.prontuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.prontuario.Prontuario;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {

	Optional<Prontuario> findByPacienteId(Long pacienteId);

}
