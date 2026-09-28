package com.msp.odonto_pro.dto.paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.msp.odonto_pro.entity.consulta.Consulta;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PacienteDTO {

	private Long id;

	@NotBlank(message = "nome é obrigatório")
	private String nome;

	private String cpf;

	private String telefone;

	private String email;

	private LocalDate dataNascimento;

	private String endereco;

	private String sexo;

	private Long convenioId;

	/** Nome do convênio, resolvido via convenioId (ou texto legado). Somente leitura. */
	private String convenio;

	private String numeroCarteirinha;

	private String responsavelNome;

	private String responsavelCpf;

	private String contatoEmergenciaNome;

	private String contatoEmergenciaTelefone;

	private String observacoes;

	private String cep;

	private String logradouro;

	private String numero;

	private String complemento;

	private String bairro;

	private String cidade;

	private String uf;

	/** Somente leitura — gerenciada via POST/DELETE /paciente/{id}/foto. */
	private String fotoUrl;

	/** Nula para pacientes cadastrados antes desta feature. */
	private LocalDateTime criadoEm;

	private List<Consulta> consultas;

	/** Consentimento LGPD para tratamento de dados, coletado no cadastro. */
	private Boolean consentimentoLgpd;

	private LocalDateTime consentimentoLgpdEm;

	/** Somente leitura — ver DELETE /pacientes/{id}/dados-pessoais. */
	private Boolean dadosAnonimizados;

	private LocalDateTime dadosAnonimizadosEm;

	/** Somente leitura — ver POST /recall/enviar. */
	private LocalDateTime ultimoRecallEnviadoEm;
}
