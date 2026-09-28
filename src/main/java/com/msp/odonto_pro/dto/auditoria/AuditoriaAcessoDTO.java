package com.msp.odonto_pro.dto.auditoria;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditoriaAcessoDTO {

	private Long id;

	private Long usuarioId;

	private String usuarioNome;

	private Long pacienteId;

	private String pacienteNome;

	/** "visualizou_prontuario" | "exportou_dados" | "excluiu_dados". */
	private String acao;

	private LocalDateTime criadoEm;

	private String ip;
}
