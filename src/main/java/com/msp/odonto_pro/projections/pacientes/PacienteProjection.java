package com.msp.odonto_pro.projections.pacientes;

import java.time.LocalDate;

public interface PacienteProjection {

	Long getId();

	String getNome();

	String getEmail();

	String getCpf();

	LocalDate getDataNascimento();

	String getTelefone();

}
