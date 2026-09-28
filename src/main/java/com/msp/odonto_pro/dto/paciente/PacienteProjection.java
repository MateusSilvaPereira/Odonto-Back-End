package com.msp.odonto_pro.dto.paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PacienteProjection {

	Long getId();

	String getNome();

	String getEmail();

	String getCpf();

	LocalDate getDataNascimento();

	String getTelefone();

	Long getConvenioId();

	String getConvenio();

	String getFotoUrl();

	LocalDateTime getCriadoEm();

}
