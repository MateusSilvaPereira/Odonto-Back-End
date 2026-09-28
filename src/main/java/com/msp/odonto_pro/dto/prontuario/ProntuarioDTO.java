package com.msp.odonto_pro.dto.prontuario;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProntuarioDTO {

	private String anamnese;
	private String historicoMedico;
	private String alergias;
	private String medicamentosUso;
	private LocalDateTime atualizadoEm;
	private String atualizadoPor;
}
