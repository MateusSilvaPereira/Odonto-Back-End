package com.msp.odonto_pro.entity.recall;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuração da campanha de recall (reativação de pacientes) — uma linha
 * por clínica, ao contrário do NotificacaoConfig legado (que é um singleton
 * global; ver nota lá). Criada sob demanda no primeiro GET/PUT de cada
 * clínica.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_RECALL_CONFIG, uniqueConstraints = @UniqueConstraint(columnNames = "clinica_id"))
public class RecallConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "clinica_id", nullable = false)
	private Long clinicaId;

	@Column(nullable = false)
	@Builder.Default
	private boolean ativoWhatsapp = false;

	@Column(nullable = false)
	@Builder.Default
	private boolean ativoEmail = false;

	@Column(columnDefinition = "TEXT")
	private String mensagem;

	private String assuntoEmail;
}
