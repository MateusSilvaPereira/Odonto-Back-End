package com.msp.odonto_pro.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dtos.ItemTratamentoDTO;
import com.msp.odonto_pro.entity.ItemTratamento;
import com.msp.odonto_pro.entity.Paciente;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repositorys.ItemTratamentoRepository;
import com.msp.odonto_pro.repositorys.PacienteRepository;
import com.msp.odonto_pro.repositorys.PagamentoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemTratamentoService {

	private static final Set<String> STATUS_VALIDOS = Set.of("orcado", "aprovado", "em_andamento", "concluido",
			"cancelado");

	private final ItemTratamentoRepository itemTratamentoRepository;
	private final PacienteRepository pacienteRepository;
	private final PagamentoRepository pagamentoRepository;

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

		return this.toDto(this.itemTratamentoRepository.save(item));
	}

	public ItemTratamentoDTO atualizar(final Long id, final ItemTratamentoDTO dto) {
		this.validarStatus(dto.getStatus());

		final ItemTratamento item = this.itemTratamentoRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Item de tratamento com id: " + id + " não encontrado!"));

		item.setDente(dto.getDente());
		item.setProcedimento(dto.getProcedimento());
		item.setValor(dto.getValor());
		item.setStatus(dto.getStatus());
		item.setObservacao(dto.getObservacao());

		return this.toDto(this.itemTratamentoRepository.save(item));
	}

	public void excluir(final Long id) {
		if (!this.itemTratamentoRepository.existsById(id)) {
			throw new NotFoundException("Item de tratamento com id: " + id + " não encontrado!");
		}

		final double totalPago = this.pagamentoRepository.somarPorItemTratamento(id);
		if (totalPago > 0) {
			throw new IllegalArgumentException(
					"Não é possível excluir um item de tratamento com pagamentos registrados. Marque o status como 'cancelado' em vez de excluir.");
		}

		this.itemTratamentoRepository.deleteById(id);
	}

	private Paciente exigirPaciente(final Long pacienteId) {
		return this.pacienteRepository.findById(pacienteId)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));
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
				.valorPago(item.getValorPago() != null ? item.getValorPago() : 0.0).build();
	}
}
