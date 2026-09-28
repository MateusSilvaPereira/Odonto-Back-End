package com.msp.odonto_pro.repository.evolucaoclinica;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.evolucaoclinica.EvolucaoClinica;

@Repository
public interface EvolucaoClinicaRepository extends JpaRepository<EvolucaoClinica, Long> {

	List<EvolucaoClinica> findByPacienteIdOrderByDataAscCriadoEmAsc(Long pacienteId);

	boolean existsByIdAndPacienteId(Long id, Long pacienteId);

}
