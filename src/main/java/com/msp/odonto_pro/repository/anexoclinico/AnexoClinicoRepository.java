package com.msp.odonto_pro.repository.anexoclinico;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.anexoclinico.AnexoClinico;

@Repository
public interface AnexoClinicoRepository extends JpaRepository<AnexoClinico, Long> {

	List<AnexoClinico> findByPacienteIdOrderByCriadoEmDesc(Long pacienteId);

}
