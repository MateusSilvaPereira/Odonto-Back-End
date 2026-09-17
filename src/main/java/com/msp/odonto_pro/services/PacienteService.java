package com.msp.odonto_pro.services;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.msp.odonto_pro.dtos.PacienteDTO;
import com.msp.odonto_pro.entity.Paciente;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.projections.pacientes.PacienteProjection;
import com.msp.odonto_pro.repositorys.PacienteRepository;
import com.msp.odonto_pro.util.PdfReportUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PacienteService {

	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final PacienteRepository pacienteRepository;
	private final ModelMapper mapper;

	// Salvar paciente...
	public PacienteDTO save(PacienteDTO dto) {
		return mapper.map(pacienteRepository.save(mapper.map(dto, Paciente.class)), PacienteDTO.class);
	}

	// Deletar Paciente...
	public void excluir(Long id) {
		if (!this.pacienteRepository.existsById(id)) {
			throw new NotFoundException("Paciente com id: " + id + " não encontrado!");
		}

		this.pacienteRepository.deleteById(id);
	}

	// Listar pacientes
	public Page<PacienteProjection> listarPacientesPaginado(Integer pagina, Integer quantidade) {
		Sort sort = Sort.by("nome").ascending();
		PageRequest pageRequest = PageRequest.of(pagina - 1, quantidade, sort);
		return pacienteRepository.listarPacientes(pageRequest);
	}

	// Listar todos os pacientes, sem paginação (usado na tela de Relatórios)
	public List<PacienteDTO> listarTodos() {
		return this.pacienteRepository.findAll(Sort.by("nome").ascending()).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	// Gera um PDF com a listagem completa de pacientes
	public byte[] gerarRelatorioPdf() {
		final List<Paciente> pacientes = this.pacienteRepository.findAll(Sort.by("nome").ascending());

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			final Document document = PdfReportUtil.novoDocumento(out);
			document.add(PdfReportUtil.titulo("Relatório de Pacientes"));
			document.add(new com.lowagie.text.Paragraph(" "));

			final PdfPTable table = PdfReportUtil.novaTabela(new float[] { 3f, 2f, 2f, 2f, 3f }, "Nome", "CPF",
					"Telefone", "Nascimento", "Email");

			for (final Paciente paciente : pacientes) {
				PdfReportUtil.celula(table, paciente.getNome());
				PdfReportUtil.celula(table, paciente.getCpf());
				PdfReportUtil.celula(table, paciente.getTelefone());
				PdfReportUtil.celula(table,
						paciente.getDataNascimento() != null ? paciente.getDataNascimento().format(DATA_BR) : "");
				PdfReportUtil.celula(table, paciente.getEmail());
			}

			document.add(table);
			document.close();
			return out.toByteArray();
		} catch (final DocumentException | java.io.IOException e) {
			throw new RuntimeException("Erro ao gerar relatório de pacientes", e);
		}
	}

	private PacienteDTO toDto(final Paciente paciente) {
		return PacienteDTO.builder().id(paciente.getId()).nome(paciente.getNome()).cpf(paciente.getCpf())
				.telefone(paciente.getTelefone()).email(paciente.getEmail())
				.dataNascimento(paciente.getDataNascimento()).endereco(paciente.getEndereco()).build();
	}

}
