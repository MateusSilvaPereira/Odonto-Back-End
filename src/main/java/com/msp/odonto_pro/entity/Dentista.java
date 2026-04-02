package com.msp.odonto_pro.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.msp.odonto_pro.util.Const;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = Const.TB_DENTISTA)
public class Dentista {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true)
	private String cpf;

	@Column(nullable = false, unique = true)
	private String cro;

	private String telefone;

	private String email;

	private String especialidade;

	private Boolean ativo;
	
	@JsonIgnore
	@OneToMany(mappedBy = "dentista", fetch = FetchType.LAZY)
	private List<Consulta> consultas;
}