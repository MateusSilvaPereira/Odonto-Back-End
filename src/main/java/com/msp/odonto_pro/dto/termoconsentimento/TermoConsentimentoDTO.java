package com.msp.odonto_pro.dto.termoconsentimento;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TermoConsentimentoDTO {

	private Long id;

	private Long pacienteId;

	private String titulo;

	private String conteudo;

	private LocalDateTime assinadoEm;

	private String criadoPor;

	/** Entrada apenas: dataURL PNG da assinatura desenhada em tela (ex: "data:image/png;base64,..."). */
	private String assinaturaBase64;

	/** Saída apenas: preenchida quando o termo foi assinado digitalmente (ver assinaturaBase64). */
	private String assinaturaUrl;
}
