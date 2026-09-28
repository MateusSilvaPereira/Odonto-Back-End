package com.msp.odonto_pro.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Ajustes de schema que o {@code spring.jpa.hibernate.ddl-auto=update} não
 * cobre (ele nunca afrouxa uma constraint NOT NULL/UNIQUE já existente).
 * Projeto não usa Flyway/Liquibase, então esses patches pontuais rodam aqui
 * na subida da aplicação. Cada comando deve ser idempotente (seguro de
 * rodar em todo startup).
 */
@Component
@RequiredArgsConstructor
public class SchemaPatchRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(SchemaPatchRunner.class);

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(final ApplicationArguments args) {
		// O cadastro novo de dentistas (módulo de agenda) não coleta CPF;
		// o campo foi mantido na entidade só por compatibilidade com o
		// cadastro antigo, então a coluna não pode mais ser obrigatória.
		this.executarSeguro("ALTER TABLE dentistas ALTER COLUMN cpf DROP NOT NULL");

		this.corrigirColunaConteudoTermoConsentimento();
	}

	/**
	 * A entidade TermoConsentimento.conteudo usou @Lob numa primeira versão,
	 * o que no Postgres cria a coluna como "oid" (Large Object) em vez de
	 * "text" — e leitura de LOB fora de uma stream ativa quebra com "Unable
	 * to access lob stream". Corrigido para TEXT simples; este patch só
	 * recria a coluna se ela ainda estiver como oid (roda uma única vez,
	 * nunca mexe numa coluna já corrigida).
	 */
	private void corrigirColunaConteudoTermoConsentimento() {
		try {
			final String tipoAtual = this.jdbcTemplate.queryForObject(
					"SELECT data_type FROM information_schema.columns WHERE table_name = 'termos_consentimento' AND column_name = 'conteudo'",
					String.class);
			if (tipoAtual != null && !"text".equalsIgnoreCase(tipoAtual)) {
				log.warn("Coluna termos_consentimento.conteudo está como '{}', recriando como TEXT (dados antigos nessa coluna serão perdidos)", tipoAtual);
				this.jdbcTemplate.execute("ALTER TABLE termos_consentimento DROP COLUMN conteudo");
				this.jdbcTemplate.execute("ALTER TABLE termos_consentimento ADD COLUMN conteudo TEXT NOT NULL DEFAULT ''");
			}
		} catch (final Exception e) {
			log.warn("Falha ao corrigir coluna termos_consentimento.conteudo: {}", e.getMessage());
		}
	}

	private void executarSeguro(final String sql) {
		try {
			this.jdbcTemplate.execute(sql);
		} catch (final Exception e) {
			log.warn("Falha ao aplicar patch de schema ('{}'): {}", sql, e.getMessage());
		}
	}
}
