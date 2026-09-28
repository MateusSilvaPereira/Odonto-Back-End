package com.msp.odonto_pro.entity.odontograma;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registro do estado de um dente (notação FDI) de um paciente. Sparse: só
 * existe uma linha quando o dente já teve seu status alterado do padrão.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_ODONTOGRAMA_DENTE, uniqueConstraints = @UniqueConstraint(columnNames = { "paciente_id",
		"numero" }))
public class OdontogramaDente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id", nullable = false)
	private Paciente paciente;

	@Column(nullable = false, length = 3)
	private String numero;

	@Column(nullable = false)
	private String status;

	private String observacao;

	private LocalDateTime atualizadoEm;

	@PrePersist
	@PreUpdate
	public void tocar() {
		this.atualizadoEm = LocalDateTime.now();
	}
}
