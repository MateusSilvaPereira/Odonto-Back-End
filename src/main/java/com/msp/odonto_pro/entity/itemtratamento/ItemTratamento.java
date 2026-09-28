package com.msp.odonto_pro.entity.itemtratamento;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.dentista.Dentista;

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

	/** Vínculo real com o cadastro de dentistas. Nullable. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dentista_id")
	private Dentista dentista;

	/** Profissional em texto livre, de antes de existir o cadastro próprio. Só usado quando {@link #dentista} é nulo. */
	@Column(name = "profissional")
	private String profissionalLegado;

	private LocalDate dataPrevista;

	private LocalDate dataRealizada;

	/**
	 * Id do Orcamento que originou este item, quando aplicável (referência
	 * solta, sem FK — mesmo padrão de EvolucaoClinica.retificaEvolucaoId).
	 * Nunca editável via PUT /tratamentos/{id}; só setado pelo
	 * OrcamentoService ao aprovar um orçamento.
	 */
	private Long orcamentoId;

	/**
	 * Id da Consulta que originou este item (atendimento -> lançamento de
	 * procedimento realizado), quando aplicável. Referência solta (mesmo
	 * padrão de orcamentoId/EvolucaoClinica.retificaEvolucaoId), validada no
	 * service.
	 */
	private Long consultaId;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
	}

	/**
	 * Nome do profissional para exibição: vem do vínculo real quando existir,
	 * ou do texto legado para itens antigos sem vínculo.
	 */
	public String getNomeProfissional() {
		return this.dentista != null ? this.dentista.getNome() : this.profissionalLegado;
	}
}
