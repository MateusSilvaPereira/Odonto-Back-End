package com.msp.odonto_pro.service.recall;
import com.msp.odonto_pro.service.shared.WhatsAppService;
import com.msp.odonto_pro.service.shared.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.recall.RecallEnvioResultadoDTO;
import com.msp.odonto_pro.entity.consulta.Consulta;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.recall.RecallConfig;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.consulta.ConsultaRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.security.SecurityUtils;
import com.msp.odonto_pro.util.PhoneUtils;

import lombok.RequiredArgsConstructor;

/**
 * Campanha de recall (reativação): dispara agora, sob demanda (não
 * agendado), pra uma lista de pacientes escolhida no front. Ao contrário
 * dos lembretes de consulta (idempotentes, controlados por timestamp por
 * ocorrência), aqui cada chamada é uma campanha nova — reenviar pro mesmo
 * paciente é uma ação explícita do admin, não um bug.
 */
@Service
@RequiredArgsConstructor
public class RecallService {

	private static final Logger log = LoggerFactory.getLogger(RecallService.class);
	private static final String STATUS_ATENDIDO = "atendido";
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final PacienteRepository pacienteRepository;
	private final ConsultaRepository consultaRepository;
	private final RecallConfigService recallConfigService;
	private final WhatsAppService whatsAppService;
	private final EmailService emailService;

	public List<RecallEnvioResultadoDTO> enviar(final List<Long> pacienteIds) {
		if (pacienteIds == null || pacienteIds.isEmpty()) {
			throw new IllegalArgumentException("pacienteIds é obrigatório e não pode ser vazio");
		}

		final RecallConfig config = this.recallConfigService.obter();
		final Long clinicaId = SecurityUtils.clinicaIdAtual();

		return pacienteIds.stream().distinct().map(pacienteId -> this.enviarParaUm(pacienteId, config, clinicaId))
				.collect(Collectors.toList());
	}

	private RecallEnvioResultadoDTO enviarParaUm(final Long pacienteId, final RecallConfig config,
			final Long clinicaId) {
		final Paciente paciente = this.pacienteRepository.findById(pacienteId)
				.filter(p -> clinicaId.equals(p.getClinicaId()))
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));

		final String mensagem = this.preencherTemplate(config.getMensagem(), paciente);
		boolean whatsappOk = false;
		boolean emailOk = false;
		String erro = null;

		if (config.isAtivoWhatsapp()) {
			final String telefone = PhoneUtils.paraE164Brasil(paciente.getTelefone());
			if (telefone == null) {
				erro = "Paciente sem telefone válido";
			} else {
				try {
					this.whatsAppService.enviarMensagem(telefone, mensagem);
					whatsappOk = true;
				} catch (final Exception e) {
					log.error("Falha ao enviar recall WhatsApp para paciente id={}: {}", pacienteId, e.getMessage());
					erro = e.getMessage();
				}
			}
		}

		if (config.isAtivoEmail()) {
			if (paciente.getEmail() == null || paciente.getEmail().isBlank()) {
				erro = erro != null ? erro : "Paciente sem e-mail válido";
			} else {
				try {
					this.emailService.enviar(paciente.getEmail(), config.getAssuntoEmail(), mensagem);
					emailOk = true;
				} catch (final Exception e) {
					log.error("Falha ao enviar recall e-mail para paciente id={}: {}", pacienteId, e.getMessage());
					erro = erro != null ? erro : e.getMessage();
				}
			}
		}

		if (whatsappOk || emailOk) {
			paciente.setUltimoRecallEnviadoEm(LocalDateTime.now());
			this.pacienteRepository.save(paciente);
			erro = null;
		}

		return RecallEnvioResultadoDTO.builder().pacienteId(paciente.getId()).nome(paciente.getNome())
				.whatsappEnviado(whatsappOk).emailEnviado(emailOk).erro(erro).build();
	}

	private String preencherTemplate(final String template, final Paciente paciente) {
		if (template == null) {
			return "";
		}

		final Consulta ultimaConsulta = this.consultaRepository
				.findFirstByPacienteIdAndStatusOrderByDataDesc(paciente.getId(), STATUS_ATENDIDO).orElse(null);

		final String ultimaVisita = ultimaConsulta != null ? ultimaConsulta.getData().format(FORMATO_DATA) : "";
		final String diasSemContato = ultimaConsulta != null
				? String.valueOf(ChronoUnit.DAYS.between(ultimaConsulta.getData(), LocalDate.now()))
				: "";

		return template.replace("{nome}", this.nullToEmpty(paciente.getNome()))
				.replace("{diasSemContato}", diasSemContato).replace("{ultimaVisita}", ultimaVisita);
	}

	private String nullToEmpty(final String valor) {
		return valor != null ? valor : "";
	}
}
