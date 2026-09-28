package com.msp.odonto_pro.enums;

/**
 * Enum que representa todas as permissões granulares do sistema.
 * 
 * Cada permissão segue o padrão: "ação:recurso" Exemplo: "read:paciente" =
 * permissão para LER pacientes
 * 
 * Este padrão é uma convenção do Spring Security e facilita a criação de regras
 * com @PreAuthorize.
 */
public enum Permission {

	// ===== PACIENTES =====
	READ_PACIENTE("read:paciente"), WRITE_PACIENTE("write:paciente"), DELETE_PACIENTE("delete:paciente"),

	// ===== DENTISTAS =====
	READ_DENTISTA("read:dentista"), WRITE_DENTISTA("write:dentista"), DELETE_DENTISTA("delete:dentista"),

	// ===== TRATAMENTOS =====
	READ_TRATAMENTO("read:tratamento"), WRITE_TRATAMENTO("write:tratamento"), DELETE_TRATAMENTO("delete:tratamento"),

	// ===== CONSULTAS =====
	READ_CONSULTA("read:consulta"), WRITE_CONSULTA("write:consulta"), DELETE_CONSULTA("delete:consulta"),

	// ===== USUÁRIOS (geralmente só ADMIN) =====
	READ_USUARIO("read:usuario"), WRITE_USUARIO("write:usuario"), DELETE_USUARIO("delete:usuario"),

	// ===== ESTOQUE =====
	READ_ESTOQUE("read:estoque"), WRITE_ESTOQUE("write:estoque"), DELETE_ESTOQUE("delete:estoque"),

	// ===== FINANCEIRO (recebimentos, despesas e fluxo de caixa) =====
	READ_FINANCEIRO("read:financeiro"), WRITE_FINANCEIRO("write:financeiro");

	/**
	 * Valor da permissão no formato "ação:recurso" Este valor será usado nas
	 * anotações @PreAuthorize
	 */
	private final String permission;

	/**
	 * Construtor do enum.
	 * 
	 * @param permission valor da permissão no formato "ação:recurso"
	 */
	Permission(final String permission) {
		this.permission = permission;
	}

	/**
	 * Retorna o valor da permissão.
	 * 
	 * @return string no formato "ação:recurso"
	 */
	public String getPermission() {
		return this.permission;
	}
}