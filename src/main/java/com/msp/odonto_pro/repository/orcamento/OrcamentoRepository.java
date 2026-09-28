package com.msp.odonto_pro.repository.orcamento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.orcamento.Orcamento;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

	List<Orcamento> findByPacienteIdOrderByCriadoEmDesc(Long pacienteId);

	/** Usado pelo link público de aprovação (ver OrcamentoPublicoService) — não escopado por clínica de propósito. */
	Optional<Orcamento> findByLinkToken(String linkToken);

	/** Só conta pendentes que ainda não expiraram (mesma regra de exibição do status). */
	long countByStatusAndValidadeGreaterThanEqualAndPaciente_ClinicaId(String status, LocalDate hoje, Long clinicaId);

}
