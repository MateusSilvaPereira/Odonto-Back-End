package com.msp.odonto_pro.repository.estoque;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.estoque.MovimentacaoEstoque;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

	@Query("SELECT m FROM MovimentacaoEstoque m JOIN FETCH m.itemEstoque WHERE m.itemEstoque.clinicaId = :clinicaId "
			+ "ORDER BY m.data DESC")
	List<MovimentacaoEstoque> listarTodasComItem(@Param("clinicaId") Long clinicaId);

}
