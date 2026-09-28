package com.msp.odonto_pro.entity.parcela;
import com.msp.odonto_pro.entity.orcamento.Orcamento;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Parcela de um Orçamento aprovado. Gerada automaticamente em OrcamentoService.aprovar(). */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_PARCELA)
public class Parcela {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orcamento_id", nullable = false)
	private Orcamento orcamento;

	@Column(nullable = false)
	private Integer numero;

	@Column(nullable = false)
	private Integer totalParcelas;

	@Column(nullable = false)
	private Double valor;

	@Column(nullable = false)
	private LocalDate vencimento;

	/** Só guarda "pendente"/"paga"; "atrasada" é calculado on-read (ver getStatusExibido). */
	@Column(nullable = false)
	private String status;

	private LocalDateTime dataPagamento;

	/** Status para exibição: "atrasada" quando pendente e vencida, sem precisar de job. */
	public String getStatusExibido() {
		if ("paga".equals(this.status)) {
			return "paga";
		}
		if (this.vencimento != null && this.vencimento.isBefore(LocalDate.now())) {
			return "atrasada";
		}
		return this.status;
	}
}
