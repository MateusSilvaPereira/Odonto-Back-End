package com.msp.odonto_pro.entity.termoconsentimento;
import com.msp.odonto_pro.entity.paciente.Paciente;

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
 * Registro de auditoria de um termo de consentimento (LGPD) assinado
 * fisicamente pelo paciente. Append-only: sem edição/exclusão, mesmo
 * espírito de EvolucaoClinica.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_TERMO_CONSENTIMENTO)
public class TermoConsentimento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id", nullable = false)
	private Paciente paciente;

	@Column(nullable = false)
	private String titulo;

	/** Texto completo do termo no momento do registro (snapshot, não referência). */
	@Column(nullable = false, columnDefinition = "TEXT")
	private String conteudo;

	@Column(nullable = false)
	private LocalDateTime assinadoEm;

	private String criadoPor;

	/** Assinatura digital (imagem), quando o termo foi assinado em tela em vez de no papel. */
	private String assinaturaUrl;

	@PrePersist
	public void prePersist() {
		this.assinadoEm = LocalDateTime.now();
	}
}
