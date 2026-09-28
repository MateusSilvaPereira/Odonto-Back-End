package com.msp.odonto_pro.entity.bloqueioagenda;
import com.msp.odonto_pro.entity.dentista.Dentista;

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

/** Bloqueio de agenda de um dentista (férias, folga etc.), usado pelo front pra não permitir agendar nesse período. */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_BLOQUEIO_AGENDA)
public class BloqueioAgenda {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dentista_id", nullable = false)
	private Dentista dentista;

	@Column(nullable = false)
	private LocalDate dataInicio;

	@Column(nullable = false)
	private LocalDate dataFim;

	private String motivo;

	private LocalDateTime criadoEm;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
	}
}
