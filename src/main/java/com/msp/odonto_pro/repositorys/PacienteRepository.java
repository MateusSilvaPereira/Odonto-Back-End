package com.msp.odonto_pro.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long>{

}
