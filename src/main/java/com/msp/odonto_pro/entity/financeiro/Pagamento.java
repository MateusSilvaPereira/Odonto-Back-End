package com.msp.odonto_pro.entity.financeiro;
import com.msp.odonto_pro.entity.itemtratamento.ItemTratamento;

import java.time.LocalDate;
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
 * Pagamento registrado contra um item do plano de tratamento (orçamento).
 * Um ItemTratamento pode ter vários pagamentos (parcelas).
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_PAGAMENTO)
public class Pagamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_tratamento_id", nullable = false)
	private ItemTratamento itemTratamento;

	@Column(nullable = false)
	private Double valor;

	@Column(nullable = false)
	private String formaPagamento;

	@Column(nullable = false)
	private LocalDate data;

	private String observacao;

	@Column(nullable = false)
	private LocalDateTime criadoEm;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
	}
}
