package com.msp.odonto_pro.service.bloqueioagenda;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.bloqueioagenda.BloqueioAgendaDTO;
import com.msp.odonto_pro.entity.bloqueioagenda.BloqueioAgenda;
import com.msp.odonto_pro.entity.dentista.Dentista;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.bloqueioagenda.BloqueioAgendaRepository;
import com.msp.odonto_pro.repository.dentista.DentistaRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BloqueioAgendaService {

	private final BloqueioAgendaRepository bloqueioAgendaRepository;
	private final DentistaRepository dentistaRepository;

	public List<BloqueioAgendaDTO> listar() {
		return this.bloqueioAgendaRepository.listarTodosComDentista(SecurityUtils.clinicaIdAtual()).stream()
				.map(this::toDto).collect(Collectors.toList());
	}

	public BloqueioAgendaDTO criar(final BloqueioAgendaDTO dto) {
		if (dto.getDentistaId() == null) {
			throw new IllegalArgumentException("dentistaId é obrigatório");
		}
		if (dto.getDataInicio() == null || dto.getDataFim() == null) {
			throw new IllegalArgumentException("dataInicio e dataFim são obrigatórios");
		}
		if (dto.getDataFim().isBefore(dto.getDataInicio())) {
			throw new IllegalArgumentException("dataFim não pode ser anterior a dataInicio");
		}

		final Dentista dentista = this.dentistaRepository.findById(dto.getDentistaId())
				.orElseThrow(() -> new NotFoundException("Dentista com id: " + dto.getDentistaId() + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(dentista.getClinicaId())) {
			throw new NotFoundException("Dentista com id: " + dto.getDentistaId() + " não encontrado!");
		}

		final BloqueioAgenda bloqueio = BloqueioAgenda.builder().dentista(dentista).dataInicio(dto.getDataInicio())
				.dataFim(dto.getDataFim()).motivo(dto.getMotivo()).build();

		return this.toDto(this.bloqueioAgendaRepository.save(bloqueio));
	}

	public void excluir(final Long id) {
		final BloqueioAgenda bloqueio = this.bloqueioAgendaRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Bloqueio de agenda com id: " + id + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(bloqueio.getDentista().getClinicaId())) {
			throw new NotFoundException("Bloqueio de agenda com id: " + id + " não encontrado!");
		}
		this.bloqueioAgendaRepository.deleteById(id);
	}

	private BloqueioAgendaDTO toDto(final BloqueioAgenda bloqueio) {
		return BloqueioAgendaDTO.builder().id(bloqueio.getId()).dentistaId(bloqueio.getDentista().getId())
				.dataInicio(bloqueio.getDataInicio()).dataFim(bloqueio.getDataFim()).motivo(bloqueio.getMotivo())
				.criadoEm(bloqueio.getCriadoEm()).build();
	}
}
