package com.msp.odonto_pro.repository.termoconsentimento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.termoconsentimento.TermoConsentimento;

@Repository
public interface TermoConsentimentoRepository extends JpaRepository<TermoConsentimento, Long> {

	List<TermoConsentimento> findByPacienteIdOrderByAssinadoEmDesc(Long pacienteId);

}
