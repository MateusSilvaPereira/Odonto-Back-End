package com.msp.odonto_pro.repositorys;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.Pagamento;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

	@Query("SELECT p FROM Pagamento p JOIN FETCH p.itemTratamento it JOIN FETCH it.paciente "
			+ "WHERE p.data BETWEEN :inicio AND :fim ORDER BY p.data DESC, p.criadoEm DESC")
	List<Pagamento> listarPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

	@Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p")
	Double somarTodos();

	@Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.data BETWEEN :inicio AND :fim")
	Double somarPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

	@Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.itemTratamento.id = :itemTratamentoId")
	Double somarPorItemTratamento(@Param("itemTratamentoId") Long itemTratamentoId);

}
