package com.msp.odonto_pro.entity.financeiro;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Conta a pagar da clínica (despesa/fornecedor), parte do módulo de fluxo
 * de caixa do Financeiro. Independente dos pagamentos recebidos de
 * pacientes (ver Pagamento).
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_DESPESA)
public class Despesa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Clínica (tenant) dona deste registro. Ver Paciente.clinicaId. */
	@Column(name = "clinica_id")
	private Long clinicaId;

	@Column(nullable = false)
	private String descricao;

	@Column(nullable = false)
	private String categoria;

	@Column(nullable = false)
	private Double valor;

	@Column(nullable = false)
	private LocalDate dataVencimento;

	private LocalDate dataPagamento;

	/** "pendente" ou "pago". "atrasado" é calculado na leitura, não persistido. */
	@Column(nullable = false)
	private String status;

	private String fornecedor;

	private String observacao;

	@Column(nullable = false)
	private LocalDateTime criadoEm;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
		if (this.status == null) {
			this.status = "pendente";
		}
	}
}
