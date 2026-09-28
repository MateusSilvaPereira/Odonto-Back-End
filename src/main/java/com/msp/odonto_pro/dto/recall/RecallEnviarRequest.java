package com.msp.odonto_pro.dto.recall;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/** Body de {@code POST /recall/enviar}. */
@Data
public class RecallEnviarRequest {

	@NotEmpty(message = "pacienteIds é obrigatório e não pode ser vazio")
	private List<Long> pacienteIds;
}
