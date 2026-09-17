package com.msp.odonto_pro.repositorys;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.Consulta;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

	@Query("SELECT c FROM Consulta c JOIN FETCH c.paciente ORDER BY c.data ASC, c.horario ASC")
	List<Consulta> listarTodasComPaciente();

	@Query("SELECT COUNT(c) FROM Consulta c WHERE c.data BETWEEN :inicio AND :fim")
	long contarPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

	/**
	 * Consultas confirmadas que ainda precisam de pelo menos um dos dois
	 * lembretes (1 dia ou 1 hora antes). A decisão de qual lembrete enviar,
	 * e se já está na janela de tempo certa, é feita em memória pelo job.
	 */
	@Query("SELECT c FROM Consulta c JOIN FETCH c.paciente WHERE c.status = 'confirmado' "
			+ "AND (c.lembrete1DiaEnviadoEm IS NULL OR c.lembrete1HoraEnviadoEm IS NULL)")
	List<Consulta> listarElegiveisParaLembrete();

}
