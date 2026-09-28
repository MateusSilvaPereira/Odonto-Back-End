package com.msp.odonto_pro.service.parcela;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msp.odonto_pro.dto.parcela.ParcelaDTO;
import com.msp.odonto_pro.entity.itemtratamento.ItemTratamento;
import com.msp.odonto_pro.entity.financeiro.Pagamento;
import com.msp.odonto_pro.entity.parcela.Parcela;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.itemtratamento.ItemTratamentoRepository;
import com.msp.odonto_pro.repository.financeiro.PagamentoRepository;
import com.msp.odonto_pro.repository.parcela.ParcelaRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * Pagar uma parcela precisa refletir no Financeiro/Comissões, que só leem
 * de Pagamento (ver FinanceiroService/ItemTratamento.valorPago). Sem isso,
 * orçamento parcelado e quitado nunca aparece como recebido em lugar
 * nenhum. Por isso pagar() aqui gera Pagamento(s) automaticamente,
 * distribuídos proporcionalmente entre os ItemTratamento gerados na
 * aprovação do orçamento (podem ser vários itens por orçamento).
 */
@Service
@RequiredArgsConstructor
public class ParcelaService {

	private static final Logger log = LoggerFactory.getLogger(ParcelaService.class);

	private final ParcelaRepository parcelaRepository;
	private final ItemTratamentoRepository itemTratamentoRepository;
	private final PagamentoRepository pagamentoRepository;

	@Transactional
	public ParcelaDTO pagar(final Long id) {
		final Parcela parcela = this.parcelaRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Parcela com id: " + id + " não encontrada!"));
		if (!SecurityUtils.clinicaIdAtual().equals(parcela.getOrcamento().getPaciente().getClinicaId())) {
			throw new NotFoundException("Parcela com id: " + id + " não encontrada!");
		}

		if ("paga".equals(parcela.getStatus())) {
			throw new IllegalArgumentException("Esta parcela já está paga");
		}

		parcela.setStatus("paga");
		parcela.setDataPagamento(LocalDateTime.now());

		this.gerarPagamentosDaParcela(parcela);

		return this.toDto(this.parcelaRepository.save(parcela));
	}

	/**
	 * Divide o valor da parcela entre os itens do plano de tratamento
	 * originados do mesmo orçamento, proporcional ao valor de cada item.
	 * O último item absorve o resto em centavos (mesmo critério usado na
	 * geração das parcelas em OrcamentoService).
	 */
	private void gerarPagamentosDaParcela(final Parcela parcela) {
		final List<ItemTratamento> itens = this.itemTratamentoRepository
				.findByOrcamentoId(parcela.getOrcamento().getId());

		if (itens.isEmpty()) {
			log.warn(
					"Parcela id={} paga, mas nenhum ItemTratamento com orcamentoId={} foi encontrado para gerar o Pagamento correspondente.",
					parcela.getId(), parcela.getOrcamento().getId());
			return;
		}

		final double totalItens = itens.stream().mapToDouble(ItemTratamento::getValor).sum();
		if (totalItens <= 0) {
			return;
		}

		final long valorParcelaCentavos = Math.round(parcela.getValor() * 100);
		long distribuidoCentavos = 0;

		for (int i = 0; i < itens.size(); i++) {
			final ItemTratamento item = itens.get(i);
			final boolean ultimo = i == itens.size() - 1;
			final long valorItemCentavos = ultimo ? valorParcelaCentavos - distribuidoCentavos
					: Math.round(valorParcelaCentavos * (item.getValor() / totalItens));
			distribuidoCentavos += valorItemCentavos;

			if (valorItemCentavos <= 0) {
				continue;
			}

			final double valorPagamento = valorItemCentavos / 100.0;

			final Pagamento pagamento = new Pagamento();
			pagamento.setItemTratamento(item);
			pagamento.setValor(valorPagamento);
			pagamento.setFormaPagamento("parcelamento");
			pagamento.setData(LocalDate.now());
			pagamento.setObservacao(
					"Gerado automaticamente pelo pagamento da parcela " + parcela.getNumero() + "/"
							+ parcela.getTotalParcelas() + " do orçamento #" + parcela.getOrcamento().getId());
			this.pagamentoRepository.save(pagamento);

			final double jaPago = item.getValorPago() != null ? item.getValorPago() : 0.0;
			item.setValorPago(jaPago + valorPagamento);
			this.itemTratamentoRepository.save(item);
		}
	}

	private ParcelaDTO toDto(final Parcela parcela) {
		return ParcelaDTO.builder().id(parcela.getId()).orcamentoId(parcela.getOrcamento().getId())
				.numero(parcela.getNumero()).totalParcelas(parcela.getTotalParcelas()).valor(parcela.getValor())
				.vencimento(parcela.getVencimento()).status(parcela.getStatusExibido())
				.dataPagamento(parcela.getDataPagamento()).build();
	}
}
