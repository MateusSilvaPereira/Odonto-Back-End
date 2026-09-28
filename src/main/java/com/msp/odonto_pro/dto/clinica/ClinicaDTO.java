package com.msp.odonto_pro.dto.clinica;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClinicaDTO {

	private Long id;

	private String nome;

	private String cnpj;

	private String telefone;

	private String endereco;

	private String logoUrl;

	private boolean ativo;

	private LocalDateTime criadoEm;
}
