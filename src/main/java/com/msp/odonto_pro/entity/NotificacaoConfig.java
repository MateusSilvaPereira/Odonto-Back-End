package com.msp.odonto_pro.entity;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuração única e global dos lembretes automáticos por WhatsApp
 * (não é por usuário). Sempre existe no máximo uma linha, com id fixo 1.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = Const.TB_NOTIFICACAO_CONFIG)
public class NotificacaoConfig {

	@Id
	private Long id;

	@Column(nullable = false)
	private boolean ativo;

	@Column(columnDefinition = "TEXT")
	private String mensagem1Dia;

	@Column(columnDefinition = "TEXT")
	private String mensagem1Hora;
}
