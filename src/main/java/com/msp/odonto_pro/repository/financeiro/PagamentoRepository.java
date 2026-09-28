package com.msp.odonto_pro.repository.financeiro;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.financeiro.Pagamento;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

	@Query("SELECT p FROM Pagamento p JOIN FETCH p.itemTratamento it JOIN FETCH it.paciente LEFT JOIN FETCH it.dentista "
			+ "WHERE it.paciente.clinicaId = :clinicaId AND p.data BETWEEN :inicio AND :fim "
			+ "AND (:pacienteId IS NULL OR it.paciente.id = :pacienteId) "
			+ "ORDER BY p.data DESC, p.criadoEm DESC")
	List<Pagamento> listarPorPeriodo(@Param("clinicaId") Long clinicaId, @Param("inicio") LocalDate inicio,
			@Param("fim") LocalDate fim, @Param("pacienteId") Long pacienteId);

	@Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.itemTratamento.paciente.clinicaId = :clinicaId")
	Double somarTodos(@Param("clinicaId") Long clinicaId);

	@Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.itemTratamento.paciente.clinicaId = :clinicaId "
			+ "AND p.data BETWEEN :inicio AND :fim")
	Double somarPorPeriodo(@Param("clinicaId") Long clinicaId, @Param("inicio") LocalDate inicio,
			@Param("fim") LocalDate fim);

	@Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.itemTratamento.id = :itemTratamentoId")
	Double somarPorItemTratamento(@Param("itemTratamentoId") Long itemTratamentoId);

	@Query("SELECT p FROM Pagamento p JOIN FETCH p.itemTratamento it WHERE it.paciente.id = :pacienteId "
			+ "ORDER BY p.data DESC, p.criadoEm DESC")
	List<Pagamento> findByPacienteId(@Param("pacienteId") Long pacienteId);

}
