package com.msp.odonto_pro.entity.orcamento;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.entity.parcela.Parcela;
import com.msp.odonto_pro.entity.paciente.Paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Orçamento proposto ao paciente, com um ou mais itens. Um passo opcional
 * ANTES do plano de tratamento: ao ser aprovado, gera automaticamente um
 * ItemTratamento por item (ver OrcamentoService.aprovar).
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_ORCAMENTO)
public class Orcamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id", nullable = false)
	private Paciente paciente;

	private String condicaoPagamento;

	@Column(nullable = false)
	private LocalDate validade;

	/** Quantas parcelas gerar na aprovação (ver OrcamentoService.aprovar). Default 1. */
	private Integer numeroParcelas;

	/** Vencimento da 1ª parcela. Nulo = usa a data de aprovação como base. */
	private LocalDate primeiroVencimento;

	/**
	 * Só armazena pendente/aprovado/recusado. "expirado" é calculado on-read
	 * (ver OrcamentoService) a partir de validade + status, sem job agendado.
	 */
	@Column(nullable = false)
	private String status;

	@Column(nullable = false)
	private LocalDateTime criadoEm;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "criado_por_id")
	private Usuario criadoPor;

	private LocalDateTime respondidoEm;

	/**
	 * Token único e não adivinhável do link público de aprovação/recusa
	 * (ver OrcamentoPublicoController), mesmo espírito de
	 * Consulta.tokenConfirmacao. Gerado uma vez na criação, nunca muda.
	 */
	@Column(unique = true)
	private String linkToken;

	@Builder.Default
	@OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("id ASC")
	private List<OrcamentoItem> itens = new ArrayList<>();

	/** Só populado depois de aprovado (ver OrcamentoService.aprovar). */
	@Builder.Default
	@OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("numero ASC")
	private List<Parcela> parcelas = new ArrayList<>();

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
		if (this.status == null) {
			this.status = "pendente";
		}
		if (this.numeroParcelas == null) {
			this.numeroParcelas = 1;
		}
		if (this.linkToken == null) {
			this.linkToken = UUID.randomUUID().toString();
		}
	}
}
