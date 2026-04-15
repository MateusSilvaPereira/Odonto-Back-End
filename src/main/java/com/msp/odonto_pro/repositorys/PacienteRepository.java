package com.msp.odonto_pro.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.msp.odonto_pro.entity.Paciente;
import com.msp.odonto_pro.projections.pacientes.PacienteProjection;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

	@Query(value = "SELECT ID AS id, NOME AS nome, CPF AS cpf,DATA_NASCIMENTO AS dataNascimento, TELEFONE AS telefone FROM PACIENTES ", countQuery = "SELECT COUNT(*) FROM PACIENTES", nativeQuery = true)
	Page<PacienteProjection> listarPacientes(Pageable pageable);

}
