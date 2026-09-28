package com.msp.odonto_pro.entity.listaespera;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.dentista.Dentista;

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

/** Fila de espera para encaixe de consulta. */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_LISTA_ESPERA)
public class ListaEspera {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id", nullable = false)
	private Paciente paciente;

	@Column(nullable = false)
	private String procedimento;

	/** Nulo = qualquer dentista. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dentista_id")
	private Dentista dentista;

	private String observacao;

	/** "aguardando" | "agendado" | "cancelado". */
	@Column(nullable = false)
	private String status;

	@Column(nullable = false)
	private LocalDateTime criadoEm;

	@PrePersist
	public void prePersist() {
		if (this.status == null) {
			this.status = "aguardando";
		}
		this.criadoEm = LocalDateTime.now();
	}
}
