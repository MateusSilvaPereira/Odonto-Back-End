package com.msp.odonto_pro.entity.evolucaoclinica;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.entity.paciente.Paciente;

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
 * Entrada da timeline clínica do paciente. Append-only por design: não há
 * PUT/DELETE neste recurso (ver EvolucaoClinicaService/Controller). Para
 * corrigir um registro, cria-se uma nova entrada com retificaEvolucaoId
 * apontando pra entrada corrigida — o histórico nunca é editado ou apagado.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_EVOLUCAO_CLINICA)
public class EvolucaoClinica {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id", nullable = false)
	private Paciente paciente;

	@Column(nullable = false)
	private LocalDate data;

	/** Texto livre, mesmo padrão de Consulta.dentista (sem cadastro próprio ainda). */
	private String dentista;

	private String queixaPrincipal;

	private String diagnostico;

	private String procedimentoRealizado;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String evolucao;

	@Column(columnDefinition = "TEXT")
	private String prescricao;

	@Column(nullable = false)
	private LocalDateTime criadoEm;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "criado_por_id")
	private Usuario criadoPor;

	/**
	 * Id de outra EvolucaoClinica (do mesmo paciente) que esta entrada
	 * retifica. Referência solta (não é FK JPA) para manter o append-only
	 * simples; validada no service.
	 */
	private Long retificaEvolucaoId;

	/**
	 * Id da Consulta que originou este registro (fluxo "marcar como
	 * atendido" -> abrir evolução pré-preenchida), quando aplicável.
	 * Referência solta (mesmo padrão de retificaEvolucaoId), validada no
	 * service.
	 */
	private Long consultaId;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
	}
}
