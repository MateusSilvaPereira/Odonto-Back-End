package com.msp.odonto_pro.repository.listaespera;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.listaespera.ListaEspera;

@Repository
public interface ListaEsperaRepository extends JpaRepository<ListaEspera, Long> {

	@Query("SELECT le FROM ListaEspera le JOIN FETCH le.paciente LEFT JOIN FETCH le.dentista "
			+ "WHERE le.paciente.clinicaId = :clinicaId ORDER BY le.criadoEm ASC")
	List<ListaEspera> listarTodosComPacienteEDentista(@Param("clinicaId") Long clinicaId);

}
