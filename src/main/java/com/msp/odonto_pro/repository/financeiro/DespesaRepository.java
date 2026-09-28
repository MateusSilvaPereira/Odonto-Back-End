package com.msp.odonto_pro.repository.financeiro;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.financeiro.Despesa;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {

	@Query("SELECT d FROM Despesa d WHERE d.clinicaId = :clinicaId AND d.dataVencimento BETWEEN :inicio AND :fim "
			+ "ORDER BY d.dataVencimento ASC")
	List<Despesa> listarPorPeriodo(@Param("clinicaId") Long clinicaId, @Param("inicio") LocalDate inicio,
			@Param("fim") LocalDate fim);

	@Query("SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d WHERE d.clinicaId = :clinicaId AND d.status = 'pago' "
			+ "AND d.dataPagamento BETWEEN :inicio AND :fim")
	Double somarPagasPorPeriodo(@Param("clinicaId") Long clinicaId, @Param("inicio") LocalDate inicio,
			@Param("fim") LocalDate fim);

	boolean existsByIdAndClinicaId(Long id, Long clinicaId);

}
