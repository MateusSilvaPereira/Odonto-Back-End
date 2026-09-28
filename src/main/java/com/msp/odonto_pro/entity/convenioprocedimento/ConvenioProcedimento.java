package com.msp.odonto_pro.entity.convenioprocedimento;

import java.math.BigDecimal;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Valor de um procedimento para um convênio específico (tabela de preços do convênio). */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_CONVENIO_PROCEDIMENTO)
public class ConvenioProcedimento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "convenio_id", nullable = false)
	private Long convenioId;

	@Column(nullable = false)
	private String procedimento;

	@Column(nullable = false)
	private BigDecimal valor;
}
