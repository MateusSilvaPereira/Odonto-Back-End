package com.msp.odonto_pro.dtos;

import com.msp.odonto_pro.entity.Consulta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TratamentoDTO {

	private Long id;

	private String descricao;

	private Double valor;

	private Consulta consulta;
}
