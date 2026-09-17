package com.msp.odonto_pro.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.msp.odonto_pro.entity.Consulta;
import com.msp.odonto_pro.entity.NotificacaoConfig;
import com.msp.odonto_pro.repositorys.ConsultaRepository;
import com.msp.odonto_pro.util.PhoneUtils;

import lombok.RequiredArgsConstructor;

/**
 * Job que dispara os lembretes de consulta por WhatsApp.
 *
 * Roda periodicamente e, a cada execução, recalcula do zero quanto tempo
 * falta para CADA consulta confirmada — não existe um horário fixo de
 * disparo. Assim que uma consulta entra na janela de "menos de 24h" ou
 * "menos de 1h" de distância, o lembrete correspondente é enviado uma
 * única vez (idempotência garantida pelos timestamps gravados na própria
 * consulta, não por controle de lote).
 */
@Component
@RequiredArgsConstructor
public class LembreteConsultaScheduler {

	private static final Logger log = LoggerFactory.getLogger(LembreteConsultaScheduler.class);

	private static final long UM_DIA_EM_MINUTOS = 24 * 60;
	private static final long UMA_HORA_EM_MINUTOS = 60;

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

	private final ConsultaRepository consultaRepository;
	private final NotificacaoConfigService notificacaoConfigService;
	private final WhatsAppService whatsAppService;

	@Scheduled(fixedRateString = "${lembretes.consulta.intervalo-ms:300000}")
	@Transactional
	public void enviarLembretesPendentes() {
		final NotificacaoConfig config = this.notificacaoConfigService.obter();
		if (!config.isAtivo()) {
			return;
		}

		final List<Consulta> candidatas = this.consultaRepository.listarElegiveisParaLembrete();
		final LocalDateTime agora = LocalDateTime.now();

		for (final Consulta consulta : candidatas) {
			this.avaliarEEnviar(consulta, config, agora);
		}
	}

	private void avaliarEEnviar(final Consulta consulta, final NotificacaoConfig config, final LocalDateTime agora) {
		if (consulta.getData() == null || consulta.getHorario() == null) {
			return;
		}

		final LocalDateTime dataHoraConsulta = LocalDateTime.of(consulta.getData(), consulta.getHorario());
		final Duration restante = Duration.between(agora, dataHoraConsulta);

		// Consulta já passou: não faz mais sentido notificar.
		if (restante.isNegative()) {
			return;
		}

		final long minutosRestantes = restante.toMinutes();

		if (consulta.getLembrete1DiaEnviadoEm() == null && minutosRestantes <= UM_DIA_EM_MINUTOS) {
			this.enviarEregistrar(consulta, config.getMensagem1Dia(), true);
		}

		if (consulta.getLembrete1HoraEnviadoEm() == null && minutosRestantes <= UMA_HORA_EM_MINUTOS) {
			this.enviarEregistrar(consulta, config.getMensagem1Hora(), false);
		}
	}

	private void enviarEregistrar(final Consulta consulta, final String template, final boolean umDiaAntes) {
		final String telefone = PhoneUtils.paraE164Brasil(consulta.getPaciente().getTelefone());
		if (telefone == null) {
			log.warn("Consulta id={} sem telefone válido para o paciente; lembrete de {} não enviado.",
					consulta.getId(), umDiaAntes ? "1 dia" : "1 hora");
			return;
		}

		final String mensagem = this.preencherTemplate(template, consulta);

		try {
			this.whatsAppService.enviarMensagem(telefone, mensagem);

			if (umDiaAntes) {
				consulta.setLembrete1DiaEnviadoEm(LocalDateTime.now());
			} else {
				consulta.setLembrete1HoraEnviadoEm(LocalDateTime.now());
			}

			this.consultaRepository.save(consulta);
		} catch (final Exception e) {
			log.error("Falha ao enviar lembrete de {} para a consulta id={}: {}", umDiaAntes ? "1 dia" : "1 hora",
					consulta.getId(), e.getMessage());
		}
	}

	private String preencherTemplate(final String template, final Consulta consulta) {
		if (template == null) {
			return "";
		}

		final String nome = this.nullToEmpty(consulta.getPaciente().getNome());
		final String data = consulta.getData() != null ? consulta.getData().format(FORMATO_DATA) : "";
		final String hora = consulta.getHorario() != null ? consulta.getHorario().format(FORMATO_HORA) : "";
		final String procedimento = this.nullToEmpty(consulta.getProcedimento());
		final String dentista = this.nullToEmpty(consulta.getDentista());

		return template.replace("{nome}", nome).replace("{data}", data).replace("{hora}", hora)
				.replace("{procedimento}", procedimento).replace("{dentista}", dentista);
	}

	private String nullToEmpty(final String valor) {
		return valor != null ? valor : "";
	}
}
