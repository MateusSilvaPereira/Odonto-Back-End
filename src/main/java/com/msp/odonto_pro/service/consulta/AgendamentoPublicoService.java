package com.msp.odonto_pro.service.consulta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msp.odonto_pro.dto.consulta.PublicAgendamentoRequest;
import com.msp.odonto_pro.dto.consulta.PublicAgendamentoResponseDTO;
import com.msp.odonto_pro.dto.consulta.PublicDentistaDTO;
import com.msp.odonto_pro.entity.consulta.Consulta;
import com.msp.odonto_pro.entity.dentista.Dentista;
import com.msp.odonto_pro.entity.dentista.HorarioAtendimento;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.exceptions.ConflitoException;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.bloqueioagenda.BloqueioAgendaRepository;
import com.msp.odonto_pro.repository.clinica.ClinicaRepository;
import com.msp.odonto_pro.repository.consulta.ConsultaRepository;
import com.msp.odonto_pro.repository.dentista.DentistaRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;

import lombok.RequiredArgsConstructor;

/**
 * Agendamento online público (sem autenticação), sob {@code /public/clinicas
 * /{clinicaId}/...}. O {@code clinicaId} vem sempre da URL — nunca inferido
 * do usuário autenticado, porque aqui não existe usuário autenticado.
 *
 * A disponibilidade é sempre recalculada no servidor (nunca confia no que o
 * front mostrou) para não expor consultas de outros pacientes e para evitar
 * conflito de horário em POST /agendamentos.
 */
@Service
@RequiredArgsConstructor
public class AgendamentoPublicoService {

	private static final int INTERVALO_SLOT_MINUTOS = 30;
	private static final int DURACAO_PADRAO_MINUTOS = 30;
	private static final String STATUS_CANCELADO = "cancelado";
	private static final String STATUS_INICIAL = "pendente";

	private final ClinicaRepository clinicaRepository;
	private final DentistaRepository dentistaRepository;
	private final BloqueioAgendaRepository bloqueioAgendaRepository;
	private final ConsultaRepository consultaRepository;
	private final PacienteRepository pacienteRepository;

	public List<PublicDentistaDTO> listarDentistas(final Long clinicaId) {
		this.exigirClinicaAtiva(clinicaId);
		return this.dentistaRepository.findByClinicaIdAndAtivoTrue(clinicaId).stream()
				.map(d -> PublicDentistaDTO.builder().id(d.getId()).nome(d.getNome()).corAgenda(d.getCorAgenda())
						.build())
				.collect(Collectors.toList());
	}

	public List<String> disponibilidade(final Long clinicaId, final Long dentistaId, final LocalDate data) {
		this.exigirClinicaAtiva(clinicaId);
		if (data == null) {
			throw new IllegalArgumentException("data é obrigatória");
		}
		final Dentista dentista = this.exigirDentista(clinicaId, dentistaId);

		if (data.isBefore(LocalDate.now())) {
			return List.of();
		}

		final boolean diaBloqueado = this.bloqueioAgendaRepository
				.existsByDentistaIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(dentistaId, data, data);
		if (diaBloqueado) {
			return List.of();
		}

		final int diaSemana = data.getDayOfWeek().getValue() % 7; // 0=domingo ... 6=sábado
		final List<HorarioAtendimento> janelas = dentista.getHorarioAtendimento().stream()
				.filter(h -> diaSemana == h.getDiaSemana()).collect(Collectors.toList());
		if (janelas.isEmpty()) {
			return List.of();
		}

		final List<Consulta> ocupadas = this.consultaRepository.findByDentistaIdAndDataAndStatusNot(dentistaId, data,
				STATUS_CANCELADO);

		final List<String> livres = new ArrayList<>();
		final LocalTime agora = data.isEqual(LocalDate.now()) ? LocalTime.now() : LocalTime.MIN;

		for (final HorarioAtendimento janela : janelas) {
			LocalTime slotInicio = janela.getHoraInicio();
			LocalTime slotFim = slotInicio.plusMinutes(INTERVALO_SLOT_MINUTOS);

			// "!slotFim.isBefore(slotInicio)" descarta o caso em que
			// plusMinutes(...) passou da meia-noite (viraria "antes" do
			// próprio início): não deveria acontecer com horário de clínica
			// normal, mas evita loop incorreto se horaFim estiver perto de 23:59.
			while (!slotFim.isBefore(slotInicio) && !slotFim.isAfter(janela.getHoraFim())) {
				if (!slotInicio.isBefore(agora) && this.semConflito(slotInicio, ocupadas)) {
					livres.add(slotInicio.toString());
				}
				slotInicio = slotFim;
				slotFim = slotInicio.plusMinutes(INTERVALO_SLOT_MINUTOS);
			}
		}

		return livres;
	}

	@Transactional
	public PublicAgendamentoResponseDTO agendar(final Long clinicaId, final PublicAgendamentoRequest request) {
		this.exigirClinicaAtiva(clinicaId);
		this.validar(request);

		final Dentista dentista = this.exigirDentista(clinicaId, request.getDentistaId());

		final List<String> livres = this.disponibilidade(clinicaId, request.getDentistaId(), request.getData());
		if (!livres.contains(request.getHorario().toString())) {
			throw new ConflitoException("Este horário não está mais disponível. Escolha outro horário.");
		}

		final Paciente paciente = this.pacienteRepository.findByCpfAndClinicaId(request.getCpf(), clinicaId)
				.orElseGet(() -> this.criarPaciente(clinicaId, request));

		final Consulta consulta = new Consulta();
		consulta.setPaciente(paciente);
		consulta.setDentista(dentista);
		consulta.setData(request.getData());
		consulta.setHorario(request.getHorario());
		consulta.setProcedimento(request.getProcedimento());
		consulta.setDuracaoMinutos(DURACAO_PADRAO_MINUTOS);
		consulta.setStatus(STATUS_INICIAL);
		consulta.setTokenConfirmacao(UUID.randomUUID().toString());
		consulta.setTokenExpiraEm(LocalDateTime.of(request.getData(), request.getHorario()).plusHours(2));

		final Consulta salva;
		try {
			salva = this.consultaRepository.save(consulta);
		} catch (final DataIntegrityViolationException e) {
			// Última linha de defesa contra duas requisições concorrentes
			// para o mesmo dentista/data/hora — a checagem de disponibilidade
			// acima roda antes do insert e não impede a corrida sozinha; o
			// índice único parcial em SchemaPatchRunner é quem garante isso.
			throw new ConflitoException("Este horário acabou de ser preenchido, escolha outro horário.");
		}

		return PublicAgendamentoResponseDTO.builder().consultaId(salva.getId()).data(salva.getData())
				.horario(salva.getHorario()).procedimento(salva.getProcedimento()).dentista(dentista.getNome())
				.status(salva.getStatus()).build();
	}

	private Paciente criarPaciente(final Long clinicaId, final PublicAgendamentoRequest request) {
		final Paciente paciente = new Paciente();
		paciente.setClinicaId(clinicaId);
		paciente.setNome(request.getNome());
		paciente.setCpf(request.getCpf());
		paciente.setTelefone(request.getTelefone());
		paciente.setEmail(request.getEmail());
		paciente.setDataNascimento(request.getDataNascimento());
		return this.pacienteRepository.save(paciente);
	}

	private boolean semConflito(final LocalTime slotInicio, final List<Consulta> ocupadas) {
		final LocalTime slotFim = slotInicio.plusMinutes(INTERVALO_SLOT_MINUTOS);
		for (final Consulta consulta : ocupadas) {
			final int duracao = consulta.getDuracaoMinutos() != null ? consulta.getDuracaoMinutos()
					: DURACAO_PADRAO_MINUTOS;
			final LocalTime ocupadoInicio = consulta.getHorario();
			final LocalTime ocupadoFim = ocupadoInicio.plusMinutes(duracao);
			if (slotInicio.isBefore(ocupadoFim) && slotFim.isAfter(ocupadoInicio)) {
				return false;
			}
		}
		return true;
	}

	private void validar(final PublicAgendamentoRequest request) {
		if (request.getNome() == null || request.getNome().isBlank()) {
			throw new IllegalArgumentException("nome é obrigatório");
		}
		if (request.getCpf() == null || request.getCpf().isBlank()) {
			throw new IllegalArgumentException("cpf é obrigatório");
		}
		if (request.getDentistaId() == null) {
			throw new IllegalArgumentException("dentistaId é obrigatório");
		}
		if (request.getData() == null || request.getHorario() == null) {
			throw new IllegalArgumentException("data e horario são obrigatórios");
		}
		if (request.getData().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("Não é possível agendar em uma data passada");
		}
		if (request.getProcedimento() == null || request.getProcedimento().isBlank()) {
			throw new IllegalArgumentException("procedimento é obrigatório");
		}
	}

	private Dentista exigirDentista(final Long clinicaId, final Long dentistaId) {
		if (dentistaId == null) {
			throw new IllegalArgumentException("dentistaId é obrigatório");
		}
		final Dentista dentista = this.dentistaRepository.findById(dentistaId)
				.orElseThrow(() -> new NotFoundException("Dentista com id: " + dentistaId + " não encontrado!"));
		if (!clinicaId.equals(dentista.getClinicaId()) || !Boolean.TRUE.equals(dentista.getAtivo())) {
			throw new NotFoundException("Dentista com id: " + dentistaId + " não encontrado!");
		}
		return dentista;
	}

	private void exigirClinicaAtiva(final Long clinicaId) {
		if (!this.clinicaRepository.existsByIdAndAtivoTrue(clinicaId)) {
			throw new NotFoundException("Clínica não encontrada");
		}
	}
}
