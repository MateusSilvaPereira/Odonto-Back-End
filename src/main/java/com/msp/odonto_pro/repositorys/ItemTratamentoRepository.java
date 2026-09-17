package com.msp.odonto_pro.repositorys;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.ItemTratamento;

@Repository
public interface ItemTratamentoRepository extends JpaRepository<ItemTratamento, Long> {

	List<ItemTratamento> findByPacienteIdOrderByCriadoEmAsc(Long pacienteId);

	@Query("SELECT COALESCE(SUM(it.valor), 0) FROM ItemTratamento it WHERE it.status <> 'cancelado'")
	Double somarValorNaoCancelado();

	@Query("SELECT it FROM ItemTratamento it JOIN FETCH it.paciente WHERE it.status <> 'cancelado'")
	List<ItemTratamento> listarNaoCanceladosComPaciente();

}
