package com.msp.odonto_pro.entity.convenio;

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

/** Convênio/plano de saúde cadastrado, vinculável a Paciente. */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_CONVENIO)
public class Convenio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Clínica (tenant) dona deste registro. Ver Paciente.clinicaId. */
	@Column(name = "clinica_id")
	private Long clinicaId;

	@Column(nullable = false)
	private String nome;

	private String registroANS;

	private String telefone;

	private String observacoes;

	@Builder.Default
	private Boolean ativo = true;

	private LocalDateTime criadoEm;

	@PrePersist
	public void prePersist() {
		if (this.ativo == null) {
			this.ativo = true;
		}
		this.criadoEm = LocalDateTime.now();
	}
}
