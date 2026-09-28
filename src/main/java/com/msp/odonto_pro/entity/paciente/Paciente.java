package com.msp.odonto_pro.entity.paciente;
import com.msp.odonto_pro.entity.convenio.Convenio;
import com.msp.odonto_pro.entity.consulta.Consulta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.msp.odonto_pro.util.Const;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = Const.TB_PACIENTE)
public class Paciente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Clínica (tenant) dona deste registro — derivada do usuário autenticado
	 * na criação (nunca do payload enviado pelo front), usada para isolar
	 * dados entre clínicas. Nullable só pra registros pré-multi-clínica antes
	 * da migração de backfill rodar (ver DataMigrationRunner).
	 */
	@Column(name = "clinica_id")
	private Long clinicaId;

	private String nome;

	private String cpf;

	private String telefone;

	private String email;

	private LocalDate dataNascimento;

	/** Legado (endereço em texto livre). Mantido para compatibilidade; o cadastro atual usa os campos estruturados abaixo. */
	private String endereco;

	private String sexo;

	/** Vínculo real com o cadastro de convênios. Nullable: paciente pode ser particular. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "convenio_id")
	private Convenio convenio;

	/** Nome do convênio em texto livre, de antes de existir o cadastro próprio. Só usado quando {@link #convenio} é nulo. */
	@Column(name = "convenio")
	private String convenioLegado;

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

	/** Gerenciada só pelo endpoint dedicado /paciente/{id}/foto (ver PacienteController). */
	private String fotoUrl;

	/**
	 * Nula para pacientes cadastrados antes desta feature (não há como saber
	 * retroativamente quando foram criados). Preenchida automaticamente daqui
	 * pra frente.
	 */
	private LocalDateTime criadoEm;

	/** Consentimento LGPD para tratamento de dados, coletado no cadastro. */
	private Boolean consentimentoLgpd;

	private LocalDateTime consentimentoLgpdEm;

	/** true após DELETE /pacientes/{id}/dados-pessoais (ver PacienteLgpdService). */
	@ColumnDefault("false")
	@Column(nullable = false)
	@Builder.Default
	private boolean dadosAnonimizados = false;

	private LocalDateTime dadosAnonimizadosEm;

	/** Atualizado a cada envio de recall bem-sucedido (ver RecallService.enviar). */
	private LocalDateTime ultimoRecallEnviadoEm;

	@JsonIgnore
	@OneToMany(mappedBy = "paciente", fetch = FetchType.LAZY)
	private List<Consulta> consultas;

	@PrePersist
	public void prePersist() {
		if (this.criadoEm == null) {
			this.criadoEm = LocalDateTime.now();
		}
	}

	/**
	 * Nome do convênio para exibição: vem do vínculo real quando existir, ou
	 * do texto legado para pacientes antigos sem vínculo.
	 */
	public String getNomeConvenio() {
		return this.convenio != null ? this.convenio.getNome() : this.convenioLegado;
	}
}
