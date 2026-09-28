package com.msp.odonto_pro.repository.configuracaoclinica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.configuracaoclinica.ConfiguracaoClinica;

@Repository
public interface ConfiguracaoClinicaRepository extends JpaRepository<ConfiguracaoClinica, Long> {

}
