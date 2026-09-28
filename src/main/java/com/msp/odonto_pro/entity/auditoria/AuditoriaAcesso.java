package com.msp.odonto_pro.entity.auditoria;

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
 * Registro de auditoria (LGPD) de acesso a ações sensíveis sobre dados de
 * paciente: visualização de prontuário completo, exportação e exclusão/
 * anonimização de dados. Append-only — sem edição/exclusão. Usuário e
 * paciente são denormalizados (id + nome) porque o registro precisa
 * continuar legível mesmo que o usuário ou o paciente sejam removidos
 * depois.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_AUDITORIA_ACESSO)
public class AuditoriaAcesso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "clinica_id", nullable = false)
	private Long clinicaId;

	@Column(name = "usuario_id", nullable = false)
	private Long usuarioId;

	@Column(nullable = false)
	private String usuarioNome;

	@Column(name = "paciente_id", nullable = false)
	private Long pacienteId;

	@Column(nullable = false)
	private String pacienteNome;

	/** "visualizou_prontuario" | "exportou_dados" | "excluiu_dados". */
	@Column(nullable = false)
	private String acao;

	@Column(nullable = false)
	private LocalDateTime criadoEm;

	private String ip;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
	}
}
