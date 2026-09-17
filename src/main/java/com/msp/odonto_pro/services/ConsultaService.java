package com.msp.odonto_pro.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.msp.odonto_pro.dtos.ConsultaDTO;
import com.msp.odonto_pro.entity.Consulta;
import com.msp.odonto_pro.entity.Paciente;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repositorys.ConsultaRepository;
import com.msp.odonto_pro.repositorys.PacienteRepository;
import com.msp.odonto_pro.util.PdfReportUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultaService {

	private static final Set<String> STATUS_VALIDOS = Set.of("confirmado", "pendente", "cancelado");
	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final ConsultaRepository consultaRepository;
	private final PacienteRepository pacienteRepository;

	public List<ConsultaDTO> listar() {
		return this.consultaRepository.listarTodasComPaciente().stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	public ConsultaDTO criar(final ConsultaDTO dto) {
		this.validarStatus(dto.getStatus());

		if (dto.getPacienteId() == null) {
			throw new IllegalArgumentException("pacienteId é obrigatório");
		}

		final Paciente paciente = this.pacienteRepository.findById(dto.getPacienteId())
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + dto.getPacienteId() + " não encontrado!"));

		final Consulta consulta = new Consulta();
		consulta.setPaciente(paciente);
		consulta.setData(dto.getData());
		consulta.setHorario(dto.getHorario());
		consulta.setProcedimento(dto.getProcedimento());
		consulta.setDentista(dto.getDentista());
		consulta.setStatus(dto.getStatus());

		return this.toDto(this.consultaRepository.save(consulta));
	}

	public ConsultaDTO atualizarStatus(final Long id, final String status) {
		this.validarStatus(status);

		final Consulta consulta = this.consultaRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Consulta com id: " + id + " não encontrada!"));

		consulta.setStatus(status);
		return this.toDto(this.consultaRepository.save(consulta));
	}

	public void excluir(final Long id) {
		if (!this.consultaRepository.existsById(id)) {
			throw new NotFoundException("Consulta com id: " + id + " não encontrada!");
		}

		this.consultaRepository.deleteById(id);
	}

	// Gera um PDF com a listagem completa de consultas
	public byte[] gerarRelatorioPdf() {
		final List<Consulta> consultas = this.consultaRepository.listarTodasComPaciente();

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			final Document document = PdfReportUtil.novoDocumento(out);
			document.add(PdfReportUtil.titulo("Relatório de Consultas"));
			document.add(new Paragraph(" "));

			final PdfPTable table = PdfReportUtil.novaTabela(new float[] { 3f, 1.5f, 1.5f, 2.5f, 2f, 1.5f },
					"Paciente", "Data", "Horário", "Procedimento", "Dentista", "Status");

			for (final Consulta consulta : consultas) {
				PdfReportUtil.celula(table, consulta.getPaciente().getNome());
				PdfReportUtil.celula(table, consulta.getData() != null ? consulta.getData().format(DATA_BR) : "");
				PdfReportUtil.celula(table, consulta.getHorario() != null ? consulta.getHorario().toString() : "");
				PdfReportUtil.celula(table, consulta.getProcedimento());
				PdfReportUtil.celula(table, consulta.getDentista());
				PdfReportUtil.celula(table, consulta.getStatus());
			}

			document.add(table);
			document.close();
			return out.toByteArray();
		} catch (final DocumentException | IOException e) {
			throw new RuntimeException("Erro ao gerar relatório de consultas", e);
		}
	}

	private void validarStatus(final String status) {
		if (status == null || !STATUS_VALIDOS.contains(status)) {
			throw new IllegalArgumentException("Status inválido. Use: confirmado, pendente ou cancelado");
		}
	}

	private ConsultaDTO toDto(final Consulta consulta) {
		return ConsultaDTO.builder().id(consulta.getId()).pacienteId(consulta.getPaciente().getId())
				.nomePaciente(consulta.getPaciente().getNome()).data(consulta.getData())
				.horario(consulta.getHorario()).procedimento(consulta.getProcedimento())
				.dentista(consulta.getDentista()).status(consulta.getStatus())
				.lembrete1DiaEnviadoEm(consulta.getLembrete1DiaEnviadoEm())
				.lembrete1HoraEnviadoEm(consulta.getLembrete1HoraEnviadoEm()).build();
	}

}
