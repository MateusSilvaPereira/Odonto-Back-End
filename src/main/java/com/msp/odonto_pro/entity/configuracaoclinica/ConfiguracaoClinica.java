package com.msp.odonto_pro.entity.configuracaoclinica;

import java.time.LocalDateTime;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dados da clínica, usados no cabeçalho de documentos impressos
 * (receituário, atestado, termo de consentimento). Singleton: sempre uma
 * única linha, com id fixo (ver ConfiguracaoClinicaService.ID_UNICO).
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_CONFIGURACAO_CLINICA)
public class ConfiguracaoClinica {

	@Id
	private Long id;

	@Column(nullable = false)
	private String nome;

	private String cnpj;

	private String telefone;

	private String endereco;

	private String logoUrl;

	private LocalDateTime atualizadoEm;

	@PrePersist
	@PreUpdate
	public void marcarAtualizacao() {
		this.atualizadoEm = LocalDateTime.now();
	}
}
