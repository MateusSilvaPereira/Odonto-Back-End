package com.msp.odonto_pro.service.orcamento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msp.odonto_pro.dto.orcamento.OrcamentoDTO;
import com.msp.odonto_pro.dto.orcamento.OrcamentoItemDTO;
import com.msp.odonto_pro.dto.parcela.ParcelaDTO;
import com.msp.odonto_pro.entity.itemtratamento.ItemTratamento;
import com.msp.odonto_pro.entity.orcamento.Orcamento;
import com.msp.odonto_pro.entity.orcamento.OrcamentoItem;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.parcela.Parcela;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.itemtratamento.ItemTratamentoRepository;
import com.msp.odonto_pro.repository.orcamento.OrcamentoRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * Orçamento: passo opcional antes do plano de tratamento. Ao ser aprovado,
 * gera automaticamente um ItemTratamento por item (ver aprovar()).
 */
@Service
@RequiredArgsConstructor
public class OrcamentoService {

	private final OrcamentoRepository orcamentoRepository;
	private final PacienteRepository pacienteRepository;
	private final ItemTratamentoRepository itemTratamentoRepository;

	public List<OrcamentoDTO> listar(final Long pacienteId) {
		this.exigirPaciente(pacienteId);
		return this.orcamentoRepository.findByPacienteIdOrderByCriadoEmDesc(pacienteId).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	public OrcamentoDTO criar(final Long pacienteId, final OrcamentoDTO dto, final Usuario usuarioLogado) {
		final Paciente paciente = this.exigirPaciente(pacienteId);

		if (dto.getItens() == null || dto.getItens().isEmpty()) {
			throw new IllegalArgumentException("O orçamento precisa ter pelo menos um item");
		}
		if (dto.getValidade() == null) {
			throw new IllegalArgumentException("validade é obrigatória");
		}
		final int numeroParcelas = dto.getNumeroParcelas() != null ? dto.getNumeroParcelas() : 1;
		if (numeroParcelas < 1) {
			throw new IllegalArgumentException("numeroParcelas deve ser maior ou igual a 1");
		}

		final Orcamento orcamento = new Orcamento();
		orcamento.setPaciente(paciente);
		orcamento.setCondicaoPagamento(dto.getCondicaoPagamento());
		orcamento.setValidade(dto.getValidade());
		orcamento.setStatus("pendente");
		orcamento.setCriadoPor(usuarioLogado);
		orcamento.setNumeroParcelas(numeroParcelas);
		orcamento.setPrimeiroVencimento(dto.getPrimeiroVencimento());

		final List<OrcamentoItem> itens = dto.getItens().stream().map(itemDto -> {
			if (itemDto.getProcedimento() == null || itemDto.getProcedimento().isBlank()) {
				throw new IllegalArgumentException("procedimento é obrigatório em todos os itens do orçamento");
			}
			if (itemDto.getValor() == null || itemDto.getValor() <= 0) {
				throw new IllegalArgumentException("valor deve ser maior que zero em todos os itens do orçamento");
			}

			final OrcamentoItem item = new OrcamentoItem();
			item.setOrcamento(orcamento);
			item.setDente(itemDto.getDente());
			item.setProcedimento(itemDto.getProcedimento());
			item.setValor(itemDto.getValor());
			item.setDesconto(itemDto.getDesconto());
			return item;
		}).collect(Collectors.toList());

		orcamento.setItens(itens);

		return this.toDto(this.orcamentoRepository.save(orcamento));
	}

	@Transactional
	public OrcamentoDTO aprovar(final Long id, final Usuario usuarioLogado) {
		final Orcamento orcamento = this.exigirOrcamento(id);
		this.exigirPendenteEValido(orcamento);

		orcamento.setStatus("aprovado");
		orcamento.setRespondidoEm(LocalDateTime.now());

		for (final OrcamentoItem item : orcamento.getItens()) {
			final ItemTratamento novoItemTratamento = new ItemTratamento();
			novoItemTratamento.setPaciente(orcamento.getPaciente());
			novoItemTratamento.setDente(item.getDente());
			novoItemTratamento.setProcedimento(item.getProcedimento());
			novoItemTratamento
					.setValor(item.getValor() - (item.getDesconto() != null ? item.getDesconto() : 0.0));
			novoItemTratamento.setStatus("aprovado");
			novoItemTratamento.setOrcamentoId(orcamento.getId());
			this.itemTratamentoRepository.save(novoItemTratamento);
		}

		// orphanRemoval exige mutar a coleção já gerenciada, não substituir a referência.
		orcamento.getParcelas().clear();
		orcamento.getParcelas().addAll(this.gerarParcelas(orcamento));

		return this.toDto(this.orcamentoRepository.save(orcamento));
	}

	/** Divide o valor total em numeroParcelas, com vencimentos mensais a partir de primeiroVencimento (ou da data de aprovação). A última parcela absorve o resto em centavos da divisão. */
	private List<Parcela> gerarParcelas(final Orcamento orcamento) {
		final int numeroParcelas = orcamento.getNumeroParcelas() != null ? orcamento.getNumeroParcelas() : 1;
		final LocalDate primeiroVencimento = orcamento.getPrimeiroVencimento() != null
				? orcamento.getPrimeiroVencimento()
				: orcamento.getRespondidoEm().toLocalDate();

		final double valorTotal = this.calcularValorTotal(orcamento);
		final long totalCentavos = Math.round(valorTotal * 100);
		final long centavosPorParcela = totalCentavos / numeroParcelas;
		final long restoCentavos = totalCentavos - (centavosPorParcela * numeroParcelas);

		final List<Parcela> parcelas = new ArrayList<>();
		for (int numero = 1; numero <= numeroParcelas; numero++) {
			final long centavosDestaParcela = centavosPorParcela + (numero == numeroParcelas ? restoCentavos : 0);
			parcelas.add(Parcela.builder().orcamento(orcamento).numero(numero).totalParcelas(numeroParcelas)
					.valor(centavosDestaParcela / 100.0).vencimento(primeiroVencimento.plusMonths(numero - 1L))
					.status("pendente").build());
		}
		return parcelas;
	}

	public OrcamentoDTO recusar(final Long id) {
		final Orcamento orcamento = this.exigirOrcamento(id);
		this.exigirPendenteEValido(orcamento);

		orcamento.setStatus("recusado");
		orcamento.setRespondidoEm(LocalDateTime.now());

		return this.toDto(this.orcamentoRepository.save(orcamento));
	}

	public void excluir(final Long id) {
		final Orcamento orcamento = this.exigirOrcamento(id);

		if (!"pendente".equals(orcamento.getStatus())) {
			throw new IllegalArgumentException(
					"Só é possível excluir orçamentos ainda pendentes. Este já foi " + orcamento.getStatus() + ".");
		}

		this.orcamentoRepository.delete(orcamento);
	}

	private void exigirPendenteEValido(final Orcamento orcamento) {
		if (!"pendente".equals(orcamento.getStatus())) {
			throw new IllegalArgumentException("Este orçamento já foi respondido (" + orcamento.getStatus() + ")");
		}
		if (orcamento.getValidade().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("Este orçamento está expirado e não pode mais ser respondido");
		}
	}

	private Orcamento exigirOrcamento(final Long id) {
		final Orcamento orcamento = this.orcamentoRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Orçamento com id: " + id + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(orcamento.getPaciente().getClinicaId())) {
			throw new NotFoundException("Orçamento com id: " + id + " não encontrado!");
		}
		return orcamento;
	}

	private Paciente exigirPaciente(final Long pacienteId) {
		final Paciente paciente = this.pacienteRepository.findById(pacienteId)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(paciente.getClinicaId())) {
			throw new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!");
		}
		return paciente;
	}

	private double calcularValorTotal(final Orcamento orcamento) {
		return orcamento.getItens().stream()
				.mapToDouble(item -> item.getValor() - (item.getDesconto() != null ? item.getDesconto() : 0.0)).sum();
	}

	private OrcamentoDTO toDto(final Orcamento orcamento) {
		return OrcamentoDTO.builder().id(orcamento.getId()).pacienteId(orcamento.getPaciente().getId())
				.itens(orcamento.getItens().stream().map(this::toItemDto).collect(Collectors.toList()))
				.valorTotal(this.calcularValorTotal(orcamento)).condicaoPagamento(orcamento.getCondicaoPagamento())
				.validade(orcamento.getValidade()).status(this.calcularStatusExibido(orcamento))
				.criadoEm(orcamento.getCriadoEm())
				.criadoPor(orcamento.getCriadoPor() != null ? orcamento.getCriadoPor().getNome() : null)
				.respondidoEm(orcamento.getRespondidoEm()).numeroParcelas(orcamento.getNumeroParcelas())
				.primeiroVencimento(orcamento.getPrimeiroVencimento())
				.parcelas(orcamento.getParcelas() == null || orcamento.getParcelas().isEmpty() ? null
						: orcamento.getParcelas().stream().map(this::toParcelaDto).collect(Collectors.toList()))
				.linkToken(orcamento.getLinkToken()).build();
	}

	private ParcelaDTO toParcelaDto(final Parcela parcela) {
		return ParcelaDTO.builder().id(parcela.getId()).orcamentoId(parcela.getOrcamento().getId())
				.numero(parcela.getNumero()).totalParcelas(parcela.getTotalParcelas()).valor(parcela.getValor())
				.vencimento(parcela.getVencimento()).status(parcela.getStatusExibido())
				.dataPagamento(parcela.getDataPagamento()).build();
	}

	private String calcularStatusExibido(final Orcamento orcamento) {
		if ("pendente".equals(orcamento.getStatus()) && orcamento.getValidade().isBefore(LocalDate.now())) {
			return "expirado";
		}
		return orcamento.getStatus();
	}

	private OrcamentoItemDTO toItemDto(final OrcamentoItem item) {
		return OrcamentoItemDTO.builder().id(item.getId()).dente(item.getDente())
				.procedimento(item.getProcedimento()).valor(item.getValor()).desconto(item.getDesconto()).build();
	}
}
