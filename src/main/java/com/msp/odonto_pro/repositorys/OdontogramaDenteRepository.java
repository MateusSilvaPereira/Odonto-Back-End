package com.msp.odonto_pro.repositorys;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.OdontogramaDente;

@Repository
public interface OdontogramaDenteRepository extends JpaRepository<OdontogramaDente, Long> {

	List<OdontogramaDente> findByPacienteId(Long pacienteId);

	Optional<OdontogramaDente> findByPacienteIdAndNumero(Long pacienteId, String numero);

}
