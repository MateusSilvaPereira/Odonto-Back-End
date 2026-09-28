package com.msp.odonto_pro.service.consulta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.msp.odonto_pro.dto.consulta.ConsultaDTO;
import com.msp.odonto_pro.dto.consulta.ReagendarConsultaRequest;
import com.msp.odonto_pro.entity.consulta.Consulta;
import com.msp.odonto_pro.entity.dentista.Dentista;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.exceptions.ConflitoException;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.consulta.ConsultaRepository;
import com.msp.odonto_pro.repository.dentista.DentistaRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.security.SecurityUtils;
import com.msp.odonto_pro.util.PdfReportUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultaService {

	private static final Set<String> STATUS_VALIDOS = Set.of("confirmado", "pendente", "cancelado", "atendido",
			"nao_compareceu");
	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final String STATUS_CANCELADO = "cancelado";

	private final ConsultaRepository consultaRepository;
	private final PacienteRepository pacienteRepository;
	private final DentistaRepository dentistaRepository;

	public List<ConsultaDTO> listar() {
		return this.consultaRepository.listarTodasComPaciente(SecurityUtils.clinicaIdAtual()).stream()
				.map(this::toDto).collect(Collectors.toList());
	}

	public ConsultaDTO criar(final ConsultaDTO dto) {
		this.validarStatus(dto.getStatus());

		if (dto.getPacienteId() == null) {
			throw new IllegalArgumentException("pacienteId é obrigatório");
		}

		if (dto.getData() == null || dto.getHorario() == null) {
			throw new IllegalArgumentException("data e horario são obrigatórios");
		}

		final Paciente paciente = this.exigirPaciente(dto.getPacienteId());
		final Dentista dentista = this.resolverDentista(dto.getDentistaId());
		final int duracaoMinutos = dto.getDuracaoMinutos() != null ? dto.getDuracaoMinutos() : 30;

		if (dentista != null) {
			this.validarConflito(dentista.getId(), dto.getData(), dto.getHorario(), duracaoMinutos, null);
		}

		final Consulta consulta = new Consulta();
		consulta.setPaciente(paciente);
		consulta.setData(dto.getData());
		consulta.setHorario(dto.getHorario());
		consulta.setProcedimento(dto.getProcedimento());
		consulta.setDentista(dentista);
		consulta.setDuracaoMinutos(duracaoMinutos);
		consulta.setStatus(dto.getStatus());
		consulta.setTokenConfirmacao(UUID.randomUUID().toString());
		consulta.setTokenExpiraEm(LocalDateTime.of(dto.getData(), dto.getHorario()).plusHours(2));

		return this.toDto(this.consultaRepository.save(consulta));
	}

	public ConsultaDTO reagendar(final Long id, final ReagendarConsultaRequest request) {
		if (request.getData() == null || request.getHorario() == null) {
			throw new IllegalArgumentException("data e horario são obrigatórios");
		}

		final Consulta consulta = this.exigir(id);
		final Dentista dentista = request.getDentistaId() != null ? this.resolverDentista(request.getDentistaId())
				: consulta.getDentista();
		final int duracaoMinutos = consulta.getDuracaoMinutos() != null ? consulta.getDuracaoMinutos() : 30;

		if (dentista != null) {
			this.validarConflito(dentista.getId(), request.getData(), request.getHorario(), duracaoMinutos, id);
		}

		consulta.setData(request.getData());
		consulta.setHorario(request.getHorario());
		consulta.setDentista(dentista);
		// Reagendada: o link de confirmação antigo não deve mais valer pro
		// horário velho; empurra a expiração a partir do novo horário.
		consulta.setTokenExpiraEm(LocalDateTime.of(request.getData(), request.getHorario()).plusHours(2));

		return this.toDto(this.consultaRepository.save(consulta));
	}

	public ConsultaDTO atualizarStatus(final Long id, final String status) {
		this.validarStatus(status);

		final Consulta consulta = this.exigir(id);

		consulta.setStatus(status);
		return this.toDto(this.consultaRepository.save(consulta));
	}

	public void excluir(final Long id) {
		this.exigir(id);
		this.consultaRepository.deleteById(id);
	}

	// Gera um PDF com a listagem completa de consultas
	public byte[] gerarRelatorioPdf() {
		final List<Consulta> consultas = this.consultaRepository.listarTodasComPaciente(SecurityUtils.clinicaIdAtual());

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
				PdfReportUtil.celula(table, consulta.getNomeDentista());
				PdfReportUtil.celula(table, consulta.getStatus());
			}

			document.add(table);
			document.close();
			return out.toByteArray();
		} catch (final DocumentException | IOException e) {
			throw new RuntimeException("Erro ao gerar relatório de consultas", e);
		}
	}

	private Dentista resolverDentista(final Long dentistaId) {
		if (dentistaId == null) {
			return null;
		}
		final Dentista dentista = this.dentistaRepository.findById(dentistaId)
				.orElseThrow(() -> new NotFoundException("Dentista com id: " + dentistaId + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(dentista.getClinicaId())) {
			throw new NotFoundException("Dentista com id: " + dentistaId + " não encontrado!");
		}
		return dentista;
	}

	/**
	 * Bloqueia duas consultas do mesmo dentista com horário sobreposto
	 * (considerando a duração de cada uma), ignorando as canceladas. Mesma
	 * regra já usada em AgendamentoPublicoService para o agendamento online.
	 *
	 * @param consultaIdExcluir id da própria consulta a ignorar na checagem
	 *                          (reagendamento); null ao criar uma nova
	 */
	private void validarConflito(final Long dentistaId, final LocalDate data, final LocalTime horario,
			final int duracaoMinutos, final Long consultaIdExcluir) {
		final LocalTime fim = horario.plusMinutes(duracaoMinutos);

		final boolean conflito = this.consultaRepository
				.findByDentistaIdAndDataAndStatusNot(dentistaId, data, STATUS_CANCELADO).stream()
				.filter(c -> consultaIdExcluir == null || !consultaIdExcluir.equals(c.getId())).anyMatch(c -> {
					final int outraDuracao = c.getDuracaoMinutos() != null ? c.getDuracaoMinutos() : 30;
					final LocalTime outroFim = c.getHorario().plusMinutes(outraDuracao);
					return horario.isBefore(outroFim) && fim.isAfter(c.getHorario());
				});

		if (conflito) {
			throw new ConflitoException("Já existe uma consulta para esse dentista nesse horário");
		}
	}

	private Paciente exigirPaciente(final Long pacienteId) {
		final Paciente paciente = this.pacienteRepository.findById(pacienteId)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(paciente.getClinicaId())) {
			throw new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!");
		}
		return paciente;
	}

	private Consulta exigir(final Long id) {
		final Consulta consulta = this.consultaRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Consulta com id: " + id + " não encontrada!"));
		if (!SecurityUtils.clinicaIdAtual().equals(consulta.getPaciente().getClinicaId())) {
			throw new NotFoundException("Consulta com id: " + id + " não encontrada!");
		}
		return consulta;
	}

	private void validarStatus(final String status) {
		if (status == null || !STATUS_VALIDOS.contains(status)) {
			throw new IllegalArgumentException(
					"Status inválido. Use: confirmado, pendente, cancelado, atendido ou nao_compareceu");
		}
	}

	private ConsultaDTO toDto(final Consulta consulta) {
		return ConsultaDTO.builder().id(consulta.getId()).pacienteId(consulta.getPaciente().getId())
				.nomePaciente(consulta.getPaciente().getNome()).pacienteFotoUrl(consulta.getPaciente().getFotoUrl())
				.data(consulta.getData()).horario(consulta.getHorario()).procedimento(consulta.getProcedimento())
				.dentistaId(consulta.getDentista() != null ? consulta.getDentista().getId() : null)
				.dentista(consulta.getNomeDentista())
				.dentistaFotoUrl(consulta.getDentista() != null ? consulta.getDentista().getFotoUrl() : null)
				.duracaoMinutos(consulta.getDuracaoMinutos())
				.status(consulta.getStatus()).lembrete1DiaEnviadoEm(consulta.getLembrete1DiaEnviadoEm())
				.lembrete1HoraEnviadoEm(consulta.getLembrete1HoraEnviadoEm())
				.lembreteEmail1DiaEnviadoEm(consulta.getLembreteEmail1DiaEnviadoEm())
				.lembreteEmail1HoraEnviadoEm(consulta.getLembreteEmail1HoraEnviadoEm()).build();
	}

}
