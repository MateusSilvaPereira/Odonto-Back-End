package com.msp.odonto_pro.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.Consulta;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

}
