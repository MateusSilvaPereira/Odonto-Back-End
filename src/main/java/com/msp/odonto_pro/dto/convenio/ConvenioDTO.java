package com.msp.odonto_pro.dto.convenio;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConvenioDTO {

	private Long id;

	private String nome;

	private String registroANS;

	private String telefone;

	private String observacoes;

	private Boolean ativo;

	private LocalDateTime criadoEm;
}
