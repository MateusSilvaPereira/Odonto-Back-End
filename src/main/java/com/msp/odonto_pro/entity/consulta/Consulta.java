package com.msp.odonto_pro.entity.consulta;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.dentista.Dentista;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_CONSULTA)
public class Consulta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id", nullable = false)
	private Paciente paciente;

	@Column(nullable = false)
	private LocalDate data;

	@Column(name = "hora", nullable = false)
	private LocalTime horario;

	private String procedimento;

	/**
	 * Vínculo real com o cadastro de dentistas. Nullable: aceita consultas
	 * sem profissional definido ainda, além das consultas antigas (ver
	 * {@link #dentistaLegado}).
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dentista_id")
	private Dentista dentista;

	/**
	 * Nome do dentista em texto livre, de antes de existir o cadastro
	 * próprio. Mantido só para consultas antigas que não têm
	 * {@link #dentista} vinculado; nunca é preenchido por consultas novas
	 * (ver ConsultaService.criar).
	 */
	@Column(name = "dentista")
	private String dentistaLegado;

	/** Duração prevista da consulta, em minutos. Null = assume 30 (ver ConsultaService). */
	private Integer duracaoMinutos;

	@Column(nullable = false)
	private String status;

	/**
	 * Timestamp de quando o lembrete de 1 dia antes foi enviado. Null =
	 * ainda não enviado. Usado para garantir idempotência por consulta.
	 */
	private LocalDateTime lembrete1DiaEnviadoEm;

	/**
	 * Timestamp de quando o lembrete de 1 hora antes foi enviado. Null =
	 * ainda não enviado.
	 */
	private LocalDateTime lembrete1HoraEnviadoEm;

	/** Mesmo controle que {@link #lembrete1DiaEnviadoEm}, canal e-mail (ver NotificacaoConfig.ativoEmail). */
	private LocalDateTime lembreteEmail1DiaEnviadoEm;

	/** Mesmo controle que {@link #lembrete1HoraEnviadoEm}, canal e-mail. */
	private LocalDateTime lembreteEmail1HoraEnviadoEm;

	/**
	 * Token único que identifica esta consulta no link público de
	 * confirmação/cancelamento enviado por WhatsApp. Gerado na criação da
	 * consulta (ver ConsultaService.criar) ou, para consultas antigas, sob
	 * demanda no primeiro lembrete disparado (ver LembreteConsultaScheduler).
	 */
	@Column(unique = true)
	private String tokenConfirmacao;

	/**
	 * Momento a partir do qual o link público deixa de aceitar ações
	 * (confirmar/cancelar). Calculado como data/hora da consulta + 2h.
	 */
	private LocalDateTime tokenExpiraEm;

	/** Timestamp de confirmação feita pelo próprio paciente via link público. */
	private LocalDateTime confirmadoViaLinkEm;

	/** Timestamp de cancelamento feito pelo próprio paciente via link público. */
	private LocalDateTime canceladoViaLinkEm;

	/**
	 * Nome do dentista para exibição: vem do vínculo real quando existir, ou
	 * do texto legado para consultas antigas sem vínculo.
	 */
	public String getNomeDentista() {
		return this.dentista != null ? this.dentista.getNome() : this.dentistaLegado;
	}
}
