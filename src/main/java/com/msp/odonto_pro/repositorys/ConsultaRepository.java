package com.msp.odonto_pro.repositorys;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.Consulta;
import com.msp.odonto_pro.projections.consultas.ConsultaResumoProjection;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

	@Query("SELECT c.id AS id, c.paciente.nome AS nomePaciente FROM Consulta c ORDER BY c.dataConsulta DESC")
	List<ConsultaResumoProjection> listarResumo();

}
