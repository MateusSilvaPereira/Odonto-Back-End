package com.msp.odonto_pro.dto.recall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Resultado do envio de recall para um paciente, um item por paciente da lista enviada. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecallEnvioResultadoDTO {

	private Long pacienteId;
	private String nome;
	private boolean whatsappEnviado;
	private boolean emailEnviado;

	/** Preenchido quando nenhum canal ligado conseguiu enviar. */
	private String erro;
}
