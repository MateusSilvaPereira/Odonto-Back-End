package com.msp.odonto_pro.entity.cobranca;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cobrança online (Pix/Boleto) contra um item de tratamento.
 *
 * MOCK: a geração de qrCode/linhaDigitavel e a confirmação de pagamento
 * (ver {@code simular-pagamento} no controller) são simuladas até a
 * integração real com o Mercado Pago entrar. O contrato de rota
 * (POST /cobrancas, GET /cobrancas/{id}) é estável e não deve mudar quando
 * a implementação interna for trocada.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_COBRANCA_ONLINE)
public class CobrancaOnline {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "item_tratamento_id", nullable = false)
	private Long itemTratamentoId;

	@Column(nullable = false)
	private Double valor;

	/** "pix" ou "boleto". */
	@Column(nullable = false)
	private String forma;

	/** "pendente" ou "pago". */
	@Column(nullable = false)
	private String status;

	/** Payload "copia e cola" fake, só para forma=pix. */
	@Column(columnDefinition = "TEXT")
	private String qrCode;

	/** Linha digitável fake, só para forma=boleto. */
	private String linhaDigitavel;

	/**
	 * Data de vencimento, só relevante para forma=boleto. Opcional: quando
	 * ausente, o front usa um fallback de "hoje + 3 dias" apenas para
	 * exibição (não persistido aqui).
	 */
	private LocalDate vencimento;

	@Column(nullable = false)
	private LocalDateTime criadoEm;

	private LocalDateTime pagoEm;

	@PrePersist
	public void prePersist() {
		this.criadoEm = LocalDateTime.now();
	}
}
