package com.msp.odonto_pro.entity.clinica;

import java.time.LocalDateTime;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Uma clínica (tenant) da plataforma. Cada {@link Usuario} não-SUPER_ADMIN
 * pertence a exatamente uma clínica (ver {@link Usuario#getClinicaId()}), e
 * todo dado operacional (pacientes, consultas, financeiro etc.) é isolado
 * por clinicaId derivado do usuário autenticado.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_CLINICA)
public class Clinica {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nome;

	private String cnpj;

	private String telefone;

	private String endereco;

	private String logoUrl;

	@Column(nullable = false)
	@Builder.Default
	private boolean ativo = true;

	private LocalDateTime criadoEm;

	private LocalDateTime atualizadoEm;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
		this.atualizadoEm = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.atualizadoEm = LocalDateTime.now();
	}
}
