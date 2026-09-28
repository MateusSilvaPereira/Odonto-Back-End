package com.msp.odonto_pro.service.consulta;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.consulta.ConsultaPublicaDTO;
import com.msp.odonto_pro.entity.consulta.Consulta;
import com.msp.odonto_pro.exceptions.LinkExpiradoException;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.consulta.ConsultaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Serviço do fluxo público (sem autenticação) de confirmação/cancelamento de
 * consulta pelo próprio paciente, via link enviado por WhatsApp. Não reusa
 * ConsultaService de propósito — este serviço nunca deve expor ou aceitar
 * nada que dependa de um usuário autenticado.
 */
@Service
@RequiredArgsConstructor
public class ConsultaPublicaService {

	private final ConsultaRepository consultaRepository;

	public ConsultaPublicaDTO buscarPorToken(final String token) {
		final Consulta consulta = this.buscarConsultaOuFalhar(token);
		return this.toDto(consulta);
	}

	public ConsultaPublicaDTO confirmarPorToken(final String token) {
		final Consulta consulta = this.buscarConsultaOuFalhar(token);
		this.garantirLinkValido(consulta);

		consulta.setStatus("confirmado");
		consulta.setConfirmadoViaLinkEm(LocalDateTime.now());

		return this.toDto(this.consultaRepository.save(consulta));
	}

	public ConsultaPublicaDTO cancelarPorToken(final String token) {
		final Consulta consulta = this.buscarConsultaOuFalhar(token);
		this.garantirLinkValido(consulta);

		consulta.setStatus("cancelado");
		consulta.setCanceladoViaLinkEm(LocalDateTime.now());

		return this.toDto(this.consultaRepository.save(consulta));
	}

	private Consulta buscarConsultaOuFalhar(final String token) {
		return this.consultaRepository.findByTokenConfirmacao(token)
				.orElseThrow(() -> new NotFoundException("Link inválido ou consulta não encontrada"));
	}

	private void garantirLinkValido(final Consulta consulta) {
		if (this.estaExpirado(consulta)) {
			throw new LinkExpiradoException("Este link não está mais disponível");
		}
	}

	private boolean estaExpirado(final Consulta consulta) {
		return consulta.getTokenExpiraEm() != null && LocalDateTime.now().isAfter(consulta.getTokenExpiraEm());
	}

	private ConsultaPublicaDTO toDto(final Consulta consulta) {
		return ConsultaPublicaDTO.builder().nomePaciente(consulta.getPaciente().getNome()).data(consulta.getData())
				.horario(consulta.getHorario()).procedimento(consulta.getProcedimento())
				.dentista(consulta.getNomeDentista()).status(consulta.getStatus())
				.linkExpirado(this.estaExpirado(consulta)).build();
	}
}
