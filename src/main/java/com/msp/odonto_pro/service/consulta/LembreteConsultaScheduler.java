package com.msp.odonto_pro.service.consulta;
import com.msp.odonto_pro.service.shared.WhatsAppService;
import com.msp.odonto_pro.service.notificacao.NotificacaoConfigService;
import com.msp.odonto_pro.service.shared.EmailService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.msp.odonto_pro.entity.consulta.Consulta;
import com.msp.odonto_pro.entity.notificacao.NotificacaoConfig;
import com.msp.odonto_pro.repository.consulta.ConsultaRepository;
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
	private final EmailService emailService;

	@Value("${app.frontend-url}")
	private String frontendUrl;

	@Scheduled(fixedRateString = "${lembretes.consulta.intervalo-ms:300000}")
	@Transactional
	public void enviarLembretesPendentes() {
		final NotificacaoConfig config = this.notificacaoConfigService.obter();
		if (!config.isAtivoWhatsapp() && !config.isAtivoEmail()) {
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

		// Cada canal (WhatsApp/e-mail) é independente: usa seu próprio par
		// mensagem/timestamp e só dispara se ligado em NotificacaoConfig,
		// mas ambos avaliam a mesma janela de "1 dia antes"/"1 hora antes".
		if (minutosRestantes <= UM_DIA_EM_MINUTOS) {
			if (config.isAtivoWhatsapp() && consulta.getLembrete1DiaEnviadoEm() == null) {
				this.enviarWhatsappEregistrar(consulta, config.getMensagem1Dia(), true);
			}
			if (config.isAtivoEmail() && consulta.getLembreteEmail1DiaEnviadoEm() == null) {
				this.enviarEmailEregistrar(consulta, config.getMensagem1Dia(), config.getAssuntoEmail1Dia(), true);
			}
		}

		if (minutosRestantes <= UMA_HORA_EM_MINUTOS) {
			if (config.isAtivoWhatsapp() && consulta.getLembrete1HoraEnviadoEm() == null) {
				this.enviarWhatsappEregistrar(consulta, config.getMensagem1Hora(), false);
			}
			if (config.isAtivoEmail() && consulta.getLembreteEmail1HoraEnviadoEm() == null) {
				this.enviarEmailEregistrar(consulta, config.getMensagem1Hora(), config.getAssuntoEmail1Hora(), false);
			}
		}
	}

	private void enviarWhatsappEregistrar(final Consulta consulta, final String template, final boolean umDiaAntes) {
		final String telefone = PhoneUtils.paraE164Brasil(consulta.getPaciente().getTelefone());
		if (telefone == null) {
			log.warn("Consulta id={} sem telefone válido para o paciente; lembrete WhatsApp de {} não enviado.",
					consulta.getId(), umDiaAntes ? "1 dia" : "1 hora");
			return;
		}

		this.garantirToken(consulta);
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
			log.error("Falha ao enviar lembrete WhatsApp de {} para a consulta id={}: {}",
					umDiaAntes ? "1 dia" : "1 hora", consulta.getId(), e.getMessage());
		}
	}

	private void enviarEmailEregistrar(final Consulta consulta, final String template, final String assunto,
			final boolean umDiaAntes) {
		final String destinatario = consulta.getPaciente().getEmail();
		if (destinatario == null || destinatario.isBlank()) {
			log.warn("Consulta id={} sem e-mail cadastrado para o paciente; lembrete e-mail de {} não enviado.",
					consulta.getId(), umDiaAntes ? "1 dia" : "1 hora");
			return;
		}

		this.garantirToken(consulta);
		final String corpo = this.preencherTemplate(template, consulta);

		try {
			this.emailService.enviar(destinatario, assunto != null ? assunto : "", corpo);

			if (umDiaAntes) {
				consulta.setLembreteEmail1DiaEnviadoEm(LocalDateTime.now());
			} else {
				consulta.setLembreteEmail1HoraEnviadoEm(LocalDateTime.now());
			}

			this.consultaRepository.save(consulta);
		} catch (final Exception e) {
			log.error("Falha ao enviar lembrete e-mail de {} para a consulta id={}: {}",
					umDiaAntes ? "1 dia" : "1 hora", consulta.getId(), e.getMessage());
		}
	}

	/**
	 * Garante que a consulta tem um token de confirmação. Consultas criadas
	 * após esta feature já vêm com token (ver ConsultaService.criar); isto
	 * cobre apenas as consultas antigas que ainda não têm um.
	 */
	private void garantirToken(final Consulta consulta) {
		if (consulta.getTokenConfirmacao() == null) {
			consulta.setTokenConfirmacao(UUID.randomUUID().toString());
			consulta.setTokenExpiraEm(LocalDateTime.of(consulta.getData(), consulta.getHorario()).plusHours(2));
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
		final String dentista = this.nullToEmpty(consulta.getNomeDentista());
		final String link = this.frontendUrl + "/confirmar-consulta/" + consulta.getTokenConfirmacao();

		return template.replace("{nome}", nome).replace("{data}", data).replace("{hora}", hora)
				.replace("{procedimento}", procedimento).replace("{dentista}", dentista).replace("{link}", link);
	}

	private String nullToEmpty(final String valor) {
		return valor != null ? valor : "";
	}
}
