package com.msp.odonto_pro.repository.consulta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.consulta.Consulta;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

	@Query("SELECT c FROM Consulta c JOIN FETCH c.paciente LEFT JOIN FETCH c.dentista "
			+ "WHERE c.paciente.clinicaId = :clinicaId ORDER BY c.data ASC, c.horario ASC")
	List<Consulta> listarTodasComPaciente(@Param("clinicaId") Long clinicaId);

	@Query("SELECT c FROM Consulta c JOIN FETCH c.paciente LEFT JOIN FETCH c.dentista WHERE c.tokenConfirmacao = :token")
	Optional<Consulta> findByTokenConfirmacao(@Param("token") String token);

	boolean existsByDentistaId(Long dentistaId);

	boolean existsByIdAndPacienteId(Long id, Long pacienteId);

	/** Última visita efetivamente realizada (status "atendido"), usada pelo recall (ver RecallService). */
	Optional<Consulta> findFirstByPacienteIdAndStatusOrderByDataDesc(Long pacienteId, String status);

	/** Consultas já ocupando a agenda do dentista naquele dia, usado pela disponibilidade pública. */
	List<Consulta> findByDentistaIdAndDataAndStatusNot(Long dentistaId, LocalDate data, String status);

	@Query("SELECT COUNT(c) FROM Consulta c WHERE c.paciente.clinicaId = :clinicaId AND c.data BETWEEN :inicio AND :fim")
	long contarPorPeriodo(@Param("clinicaId") Long clinicaId, @Param("inicio") LocalDate inicio,
			@Param("fim") LocalDate fim);

	/**
	 * Consultas confirmadas que ainda precisam de pelo menos um dos dois
	 * lembretes (1 dia ou 1 hora antes). A decisão de qual lembrete enviar,
	 * e se já está na janela de tempo certa, é feita em memória pelo job.
	 */
	@Query("SELECT c FROM Consulta c JOIN FETCH c.paciente LEFT JOIN FETCH c.dentista WHERE c.status = 'confirmado' "
			+ "AND (c.lembrete1DiaEnviadoEm IS NULL OR c.lembrete1HoraEnviadoEm IS NULL)")
	List<Consulta> listarElegiveisParaLembrete();

}
