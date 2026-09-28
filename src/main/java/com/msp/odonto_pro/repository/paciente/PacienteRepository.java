package com.msp.odonto_pro.repository.paciente;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.dto.paciente.PacienteProjection;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

	@Query(value = "SELECT p.ID AS id, p.NOME AS nome, p.EMAIL AS email, p.CPF AS cpf, p.DATA_NASCIMENTO AS dataNascimento, "
			+ "p.TELEFONE AS telefone, p.CONVENIO_ID AS convenioId, COALESCE(c.NOME, p.CONVENIO) AS convenio, "
			+ "p.FOTO_URL AS fotoUrl, p.CRIADO_EM AS criadoEm "
			+ "FROM PACIENTES p LEFT JOIN CONVENIOS c ON c.ID = p.CONVENIO_ID WHERE p.CLINICA_ID = :clinicaId",
			countQuery = "SELECT COUNT(*) FROM PACIENTES WHERE CLINICA_ID = :clinicaId", nativeQuery = true)
	Page<PacienteProjection> listarPacientes(@Param("clinicaId") Long clinicaId, Pageable pageable);

	List<Paciente> findByClinicaId(Long clinicaId);

	long countByClinicaId(Long clinicaId);

	boolean existsByIdAndClinicaId(Long id, Long clinicaId);

	boolean existsByConvenioId(Long convenioId);

	/** Usado pelo upsert do agendamento online público (ver AgendamentoPublicoService). */
	Optional<Paciente> findByCpfAndClinicaId(String cpf, Long clinicaId);

}
