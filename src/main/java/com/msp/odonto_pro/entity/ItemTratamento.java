package com.msp.odonto_pro.entity;

import java.time.LocalDateTime;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Item do plano de tratamento (orçamento) de um paciente. Não confundir com
 * a entidade legada Tratamento (ligada 1:1 a uma Consulta) — este é um
 * módulo novo e independente.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_ITEM_TRATAMENTO)
public class ItemTratamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id", nullable = false)
	private Paciente paciente;

	/**
	 * Dente em notação FDI, opcional (procedimento pode não estar ligado a um
	 * dente específico).
	 */
	private String dente;

	@Column(nullable = false)
	private String procedimento;

	@Column(nullable = false)
	private Double valor;

	@Column(nullable = false)
	private String status;

	private String observacao;

	@Column(nullable = false)
	private LocalDateTime criadoEm;

	/**
	 * Soma de todos os pagamentos já registrados para este item. Recalculado
	 * pelo FinanceiroService sempre que um Pagamento é criado — não é editado
	 * diretamente pelo CRUD de tratamento.
	 */
	@lombok.Builder.Default
	private Double valorPago = 0.0;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
	}
}
