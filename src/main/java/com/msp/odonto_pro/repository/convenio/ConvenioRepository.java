package com.msp.odonto_pro.repository.convenio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.convenio.Convenio;

@Repository
public interface ConvenioRepository extends JpaRepository<Convenio, Long> {

	List<Convenio> findByClinicaId(Long clinicaId);

	boolean existsByIdAndClinicaId(Long id, Long clinicaId);

}
