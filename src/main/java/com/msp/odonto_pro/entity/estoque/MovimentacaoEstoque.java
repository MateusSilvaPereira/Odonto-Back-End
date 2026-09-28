package com.msp.odonto_pro.entity.estoque;

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

/** Entrada ou saída registrada contra um ItemEstoque. */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_MOVIMENTACAO_ESTOQUE)
public class MovimentacaoEstoque {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_estoque_id", nullable = false)
	private ItemEstoque itemEstoque;

	/** "entrada" (compra/reposição) ou "saida" (uso/consumo). */
	@Column(nullable = false)
	private String tipo;

	@Column(nullable = false)
	private Double quantidade;

	private String motivo;

	@Column(nullable = false)
	private LocalDateTime data;

	private String criadoPor;

	@PrePersist
	public void prePersist() {
		this.data = LocalDateTime.now();
	}
}
