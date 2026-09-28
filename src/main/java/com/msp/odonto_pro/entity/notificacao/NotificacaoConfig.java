package com.msp.odonto_pro.entity.notificacao;

import org.hibernate.annotations.ColumnDefault;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuração única e global dos lembretes automáticos por WhatsApp e
 * e-mail (não é por usuário). Sempre existe no máximo uma linha, com id
 * fixo 1.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = Const.TB_NOTIFICACAO_CONFIG)
public class NotificacaoConfig {

	@Id
	private Long id;

	/** Renomeado de "ativo": a coluna antiga fica órfã no banco (inofensiva). */
	@Column(name = "ativo_whatsapp", nullable = false)
	@ColumnDefault("true")
	private boolean ativoWhatsapp;

	/** Desativado por padrão: canal novo, evita disparo sem o admin revisar assunto/corpo antes. */
	@Column(nullable = false)
	@ColumnDefault("false")
	private boolean ativoEmail;

	@Column(columnDefinition = "TEXT")
	private String mensagem1Dia;

	@Column(columnDefinition = "TEXT")
	private String mensagem1Hora;

	/** Assunto do e-mail de lembrete de 1 dia antes. Só usado quando ativoEmail=true. */
	private String assuntoEmail1Dia;

	/** Assunto do e-mail de lembrete de 1 hora antes. Só usado quando ativoEmail=true. */
	private String assuntoEmail1Hora;
}
