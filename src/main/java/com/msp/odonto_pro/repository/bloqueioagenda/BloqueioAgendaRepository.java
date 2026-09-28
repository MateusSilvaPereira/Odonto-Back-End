package com.msp.odonto_pro.repository.bloqueioagenda;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.bloqueioagenda.BloqueioAgenda;

@Repository
public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, Long> {

	@Query("SELECT b FROM BloqueioAgenda b JOIN FETCH b.dentista WHERE b.dentista.clinicaId = :clinicaId "
			+ "ORDER BY b.dataInicio ASC")
	List<BloqueioAgenda> listarTodosComDentista(@Param("clinicaId") Long clinicaId);

	/** Usado pela agenda de disponibilidade pública — dia inteiro bloqueado = sem horários. */
	boolean existsByDentistaIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(Long dentistaId, LocalDate data,
			LocalDate dataRepetida);

}
