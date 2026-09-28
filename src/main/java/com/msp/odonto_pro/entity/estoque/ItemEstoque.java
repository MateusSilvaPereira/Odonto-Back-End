package com.msp.odonto_pro.entity.estoque;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Material/insumo cadastrado no estoque da clínica. */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_ITEM_ESTOQUE)
public class ItemEstoque {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Clínica (tenant) dona deste registro. Ver Paciente.clinicaId. */
	@Column(name = "clinica_id")
	private Long clinicaId;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false)
	private String categoria;

	@Column(nullable = false)
	private String unidadeMedida;

	@Column(nullable = false)
	private Double quantidadeAtual;

	@Column(nullable = false)
	private Double quantidadeMinima;

	private String fornecedor;

	private LocalDate validade;

	private Double precoUnitario;

	@Column(nullable = false)
	private LocalDateTime atualizadoEm;

	/**
	 * Controle de concorrência otimista: sem isso, duas baixas/entradas
	 * simultâneas (ver EstoqueService.registrarMovimentacao) podem ler a
	 * mesma quantidadeAtual e uma sobrescrever a outra.
	 */
	@Version
	private Long version;

	@PrePersist
	@PreUpdate
	public void marcarAtualizacao() {
		this.atualizadoEm = LocalDateTime.now();
	}
}
