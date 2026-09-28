package com.msp.odonto_pro.repository.clinica;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.clinica.Clinica;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {

	boolean existsByCnpj(String cnpj);

	/** Usado pelos endpoints públicos de agendamento — clínica precisa existir e estar ativa. */
	boolean existsByIdAndAtivoTrue(Long id);

	Optional<Clinica> findFirstByNome(String nome);

}
