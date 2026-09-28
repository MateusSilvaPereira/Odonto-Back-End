package com.msp.odonto_pro.dto.anexoclinico;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnexoClinicoDTO {

	private Long id;

	private Long pacienteId;

	private String nome;

	private String tipo;

	private String url;

	private Long tamanho;

	private LocalDateTime criadoEm;

	private String criadoPor;
}
