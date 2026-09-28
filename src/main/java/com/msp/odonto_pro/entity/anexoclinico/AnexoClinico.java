package com.msp.odonto_pro.entity.anexoclinico;
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

/** Anexo clínico do paciente (radiografia, foto intraoral, documento etc.), armazenado em disco (ver /uploads). */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_ANEXO_CLINICO)
public class AnexoClinico {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paciente_id", nullable = false)
	private Paciente paciente;

	@Column(nullable = false)
	private String nome;

	/** "imagem" ou "documento". */
	@Column(nullable = false)
	private String tipo;

	@Column(nullable = false)
	private String url;

	private Long tamanho;

	private LocalDateTime criadoEm;

	private String criadoPor;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
	}
}
