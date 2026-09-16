package com.msp.odonto_pro.services;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.msp.odonto_pro.dtos.PacienteDTO;
import com.msp.odonto_pro.entity.Paciente;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.projections.pacientes.PacienteProjection;
import com.msp.odonto_pro.repositorys.PacienteRepository;

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
			final Document document = new Document(PageSize.A4, 36, 36, 54, 36);
			PdfWriter.getInstance(document, out);
			document.open();

			final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
			document.add(new Paragraph("Relatório de Pacientes", titleFont));
			document.add(new Paragraph(" "));

			final PdfPTable table = new PdfPTable(5);
			table.setWidthPercentage(100);
			table.setWidths(new float[] { 3f, 2f, 2f, 2f, 3f });

			final Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
			Stream.of("Nome", "CPF", "Telefone", "Nascimento", "Email")
					.forEach(text -> table.addCell(new PdfPCell(new Phrase(text, headerFont))));

			final Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
			for (final Paciente paciente : pacientes) {
				table.addCell(new Phrase(nullToEmpty(paciente.getNome()), cellFont));
				table.addCell(new Phrase(nullToEmpty(paciente.getCpf()), cellFont));
				table.addCell(new Phrase(nullToEmpty(paciente.getTelefone()), cellFont));
				table.addCell(new Phrase(
						paciente.getDataNascimento() != null ? paciente.getDataNascimento().format(DATA_BR) : "",
						cellFont));
				table.addCell(new Phrase(nullToEmpty(paciente.getEmail()), cellFont));
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

	private String nullToEmpty(final String value) {
		return value != null ? value : "";
	}

}
