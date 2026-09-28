package com.msp.odonto_pro.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlterarSenhaRequest {
	@NotBlank(message = "senhaAtual é obrigatório")
	private String senhaAtual;

	@NotBlank(message = "novaSenha é obrigatório")
	@Size(min = 6, message = "novaSenha deve ter ao menos 6 caracteres")
	private String novaSenha;
}
