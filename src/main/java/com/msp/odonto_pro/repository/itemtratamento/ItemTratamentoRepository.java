package com.msp.odonto_pro.repository.itemtratamento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.itemtratamento.ItemTratamento;

@Repository
public interface ItemTratamentoRepository extends JpaRepository<ItemTratamento, Long> {

	@Query("SELECT it FROM ItemTratamento it LEFT JOIN FETCH it.dentista WHERE it.paciente.id = :pacienteId ORDER BY it.criadoEm ASC")
	List<ItemTratamento> findByPacienteIdOrderByCriadoEmAsc(@Param("pacienteId") Long pacienteId);

	List<ItemTratamento> findByOrcamentoId(Long orcamentoId);

	boolean existsByIdAndPaciente_ClinicaId(Long id, Long clinicaId);

	@Query("SELECT COALESCE(SUM(it.valor), 0) FROM ItemTratamento it WHERE it.paciente.clinicaId = :clinicaId AND it.status <> 'cancelado'")
	Double somarValorNaoCancelado(@Param("clinicaId") Long clinicaId);

	@Query("SELECT it FROM ItemTratamento it JOIN FETCH it.paciente WHERE it.paciente.clinicaId = :clinicaId AND it.status <> 'cancelado'")
	List<ItemTratamento> listarNaoCanceladosComPaciente(@Param("clinicaId") Long clinicaId);

	long countByStatusAndPaciente_ClinicaId(String status, Long clinicaId);

}
