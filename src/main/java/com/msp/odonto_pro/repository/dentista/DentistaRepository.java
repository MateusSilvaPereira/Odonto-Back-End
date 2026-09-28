package com.msp.odonto_pro.repository.dentista;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.dentista.Dentista;

@Repository
public interface DentistaRepository extends JpaRepository<Dentista, Long> {

	List<Dentista> findByClinicaId(Long clinicaId);

	/** Usado pelo agendamento online público — só dentistas ativos aparecem pro paciente escolher. */
	List<Dentista> findByClinicaIdAndAtivoTrue(Long clinicaId);

	boolean existsByIdAndClinicaId(Long id, Long clinicaId);

	boolean existsByCroAndClinicaId(String cro, Long clinicaId);

	boolean existsByCroAndIdNotAndClinicaId(String cro, Long id, Long clinicaId);

	/** CRO é único GLOBALMENTE no banco (não só por clínica) — ver constraint em Dentista.cro. */
	boolean existsByCro(String cro);

	boolean existsByCroAndIdNot(String cro, Long id);

}
