package com.msp.odonto_pro.entity.prontuario;
import com.msp.odonto_pro.entity.usuario.Usuario;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dados de cadastro clínico do paciente (não é histórico — é editável).
 * Uma linha por paciente, criada sob demanda no primeiro PUT.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_PRONTUARIO)
public class Prontuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id", nullable = false, unique = true)
	private Paciente paciente;

	@Column(columnDefinition = "TEXT")
	private String anamnese;

	@Column(columnDefinition = "TEXT")
	private String historicoMedico;

	@Column(columnDefinition = "TEXT")
	private String alergias;

	@Column(columnDefinition = "TEXT")
	private String medicamentosUso;

	private LocalDateTime atualizadoEm;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "atualizado_por_id")
	private Usuario atualizadoPor;

	@PrePersist
	@PreUpdate
	public void tocar() {
		this.atualizadoEm = LocalDateTime.now();
	}
}
