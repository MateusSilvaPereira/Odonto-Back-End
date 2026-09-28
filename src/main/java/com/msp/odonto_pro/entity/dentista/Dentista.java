package com.msp.odonto_pro.entity.dentista;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cadastro de perfil profissional do dentista (CRO, especialidade, cor na
 * agenda etc.), independente do login (ver {@link Usuario} / Role.DENTISTA).
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_DENTISTA)
public class Dentista {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Clínica (tenant) dona deste registro. Ver Paciente.clinicaId. */
	@Column(name = "clinica_id")
	private Long clinicaId;

	@Column(nullable = false)
	private String nome;

	/**
	 * Mantido por compatibilidade com o cadastro antigo. Não faz parte do
	 * módulo novo de cadastro de dentistas (o front não envia este campo), por
	 * isso é opcional.
	 */
	private String cpf;

	@Column(nullable = false, unique = true)
	private String cro;

	private String especialidade;

	private String telefone;

	private String email;

	/** Cor usada para identificar o dentista na agenda, em hexadecimal (ex: #4f6bed). */
	private String corAgenda;

	private String observacoes;

	/** Gerenciada só pelo endpoint dedicado /dentistas/{id}/foto (ver DentistaController). */
	private String fotoUrl;

	@Builder.Default
	private Boolean ativo = true;

	private LocalDateTime criadoEm;

	/** Percentual (0-100) de comissão sobre os pagamentos recebidos dos tratamentos que ele realiza. Default 0. */
	@Builder.Default
	private Double percentualComissao = 0.0;

	/** Janelas de atendimento por dia da semana. Vazio/ausente = sem restrição de horário. */
	@ElementCollection
	@CollectionTable(name = Const.TB_DENTISTA_HORARIO_ATENDIMENTO, joinColumns = @JoinColumn(name = "dentista_id"))
	@Builder.Default
	private List<HorarioAtendimento> horarioAtendimento = new ArrayList<>();

	@PrePersist
	public void prePersist() {
		if (this.ativo == null) {
			this.ativo = true;
		}
		if (this.percentualComissao == null) {
			this.percentualComissao = 0.0;
		}
		this.criadoEm = LocalDateTime.now();
	}
}
