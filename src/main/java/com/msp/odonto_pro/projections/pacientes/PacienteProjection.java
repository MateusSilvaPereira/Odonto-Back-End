package com.msp.odonto_pro.projections.pacientes;

import java.time.LocalDate;

public interface PacienteProjection {
//Listas pacientes
	
	Long getId();
	String getNome(); 
	String getCpf();
	LocalDate getDataNascimento();
	String getTelefone();
	
}
