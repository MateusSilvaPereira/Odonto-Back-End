package com.msp.odonto_pro.service.consulta;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Component;

import com.msp.odonto_pro.exceptions.TooManyRequestsException;

/**
 * Limite de tentativas em memória para o agendamento online público (sem
 * autenticação). Simples de propósito: suficiente para uma única instância
 * da aplicação, mas NÃO sobrevive a restart nem escala pra múltiplas
 * instâncias (nesse caso precisaria de um contador compartilhado, ex.
 * Redis). Ver AgendamentoPublicoController.
 */
@Component
public class PublicBookingRateLimiter {

	private static final int MAX_TENTATIVAS_POR_IP = 5;
	private static final int MAX_TENTATIVAS_POR_CPF = 3;
	private static final Duration JANELA = Duration.ofMinutes(30);

	private final Map<String, Deque<Instant>> tentativas = new ConcurrentHashMap<>();

	public void verificar(final String ip, final String cpf) {
		this.verificarChave("ip:" + ip, MAX_TENTATIVAS_POR_IP);
		this.verificarChave("cpf:" + cpf, MAX_TENTATIVAS_POR_CPF);
	}

	private void verificarChave(final String chave, final int maximo) {
		final Deque<Instant> registros = this.tentativas.computeIfAbsent(chave, k -> new ConcurrentLinkedDeque<>());
		final Instant agora = Instant.now();

		synchronized (registros) {
			while (!registros.isEmpty() && Duration.between(registros.peekFirst(), agora).compareTo(JANELA) > 0) {
				registros.pollFirst();
			}

			if (registros.size() >= maximo) {
				throw new TooManyRequestsException(
						"Muitas tentativas de agendamento em pouco tempo. Tente novamente mais tarde.");
			}

			registros.addLast(agora);
		}
	}
}
