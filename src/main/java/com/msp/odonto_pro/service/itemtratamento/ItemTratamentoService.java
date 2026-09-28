package com.msp.odonto_pro.service.itemtratamento;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.itemtratamento.ItemTratamentoDTO;
import com.msp.odonto_pro.entity.dentista.Dentista;
import com.msp.odonto_pro.entity.itemtratamento.ItemTratamento;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.consulta.ConsultaRepository;
import com.msp.odonto_pro.repository.dentista.DentistaRepository;
import com.msp.odonto_pro.repository.itemtratamento.ItemTratamentoRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.repository.financeiro.PagamentoRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemTratamentoService {

	private static final Set<String> STATUS_VALIDOS = Set.of("orcado", "aprovado", "em_andamento", "concluido",
			"cancelado");

	private final ItemTratamentoRepository itemTratamentoRepository;
	private final PacienteRepository pacienteRepository;
	private final PagamentoRepository pagamentoRepository;
	private final DentistaRepository dentistaRepository;
	private final ConsultaRepository consultaRepository;

	public List<ItemTratamentoDTO> listarPorPaciente(final Long pacienteId) {
		this.exigirPaciente(pacienteId);
		return this.itemTratamentoRepository.findByPacienteIdOrderByCriadoEmAsc(pacienteId).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	public ItemTratamentoDTO criar(final Long pacienteId, final ItemTratamentoDTO dto) {
		final Paciente paciente = this.exigirPaciente(pacienteId);
		this.validarStatus(dto.getStatus());

		final ItemTratamento item = new ItemTratamento();
		item.setPaciente(paciente);
		item.setDente(dto.getDente());
		item.setProcedimento(dto.getProcedimento());
		item.setValor(dto.getValor());
		item.setStatus(dto.getStatus());
		item.setObservacao(dto.getObservacao());
		item.setDentista(this.resolverDentista(dto.getDentistaId()));
		item.setDataPrevista(dto.getDataPrevista());
		item.setDataRealizada(dto.getDataRealizada());
		item.setConsultaId(this.resolverConsultaId(pacienteId, dto.getConsultaId()));
		// orcamentoId nunca vem do cliente: só é setado pelo OrcamentoService

		return this.toDto(this.itemTratamentoRepository.save(item));
	}

	public ItemTratamentoDTO atualizar(final Long id, final ItemTratamentoDTO dto) {
		this.validarStatus(dto.getStatus());

		final ItemTratamento item = this.exigir(id);

		item.setDente(dto.getDente());
		item.setProcedimento(dto.getProcedimento());
		item.setValor(dto.getValor());
		item.setStatus(dto.getStatus());
		item.setObservacao(dto.getObservacao());
		item.setDentista(this.resolverDentista(dto.getDentistaId()));
		item.setDataPrevista(dto.getDataPrevista());
		item.setDataRealizada(dto.getDataRealizada());
		// orcamentoId propositalmente não é tocado aqui

		return this.toDto(this.itemTratamentoRepository.save(item));
	}

	public void excluir(final Long id) {
		this.exigir(id);

		final double totalPago = this.pagamentoRepository.somarPorItemTratamento(id);
		if (totalPago > 0) {
			throw new IllegalArgumentException(
					"Não é possível excluir um item de tratamento com pagamentos registrados. Marque o status como 'cancelado' em vez de excluir.");
		}

		this.itemTratamentoRepository.deleteById(id);
	}

	private Paciente exigirPaciente(final Long pacienteId) {
		final Paciente paciente = this.pacienteRepository.findById(pacienteId)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(paciente.getClinicaId())) {
			throw new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!");
		}
		return paciente;
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

	private Long resolverConsultaId(final Long pacienteId, final Long consultaId) {
		if (consultaId == null) {
			return null;
		}
		if (!this.consultaRepository.existsByIdAndPacienteId(consultaId, pacienteId)) {
			throw new NotFoundException(
					"Consulta com id: " + consultaId + " não encontrada para este paciente!");
		}
		return consultaId;
	}

	private ItemTratamento exigir(final Long id) {
		final ItemTratamento item = this.itemTratamentoRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Item de tratamento com id: " + id + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(item.getPaciente().getClinicaId())) {
			throw new NotFoundException("Item de tratamento com id: " + id + " não encontrado!");
		}
		return item;
	}

	private void validarStatus(final String status) {
		if (status == null || !STATUS_VALIDOS.contains(status)) {
			throw new IllegalArgumentException(
					"Status inválido. Use: orcado, aprovado, em_andamento, concluido ou cancelado");
		}
	}

	private ItemTratamentoDTO toDto(final ItemTratamento item) {
		return ItemTratamentoDTO.builder().id(item.getId()).pacienteId(item.getPaciente().getId())
				.dente(item.getDente()).procedimento(item.getProcedimento()).valor(item.getValor())
				.status(item.getStatus()).criadoEm(item.getCriadoEm()).observacao(item.getObservacao())
				.valorPago(item.getValorPago() != null ? item.getValorPago() : 0.0)
				.dentistaId(item.getDentista() != null ? item.getDentista().getId() : null)
				.profissional(item.getNomeProfissional()).dataPrevista(item.getDataPrevista())
				.dataRealizada(item.getDataRealizada()).orcamentoId(item.getOrcamentoId())
				.consultaId(item.getConsultaId()).build();
	}
}
