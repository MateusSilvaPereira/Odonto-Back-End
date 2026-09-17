package com.msp.odonto_pro.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.msp.odonto_pro.dtos.FinanceiroResumoDTO;
import com.msp.odonto_pro.dtos.PagamentoDTO;
import com.msp.odonto_pro.dtos.PendenciaTratamentoDTO;
import com.msp.odonto_pro.entity.ItemTratamento;
import com.msp.odonto_pro.entity.Pagamento;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repositorys.ItemTratamentoRepository;
import com.msp.odonto_pro.repositorys.PagamentoRepository;
import com.msp.odonto_pro.util.PdfReportUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinanceiroService {

	private static final Set<String> FORMAS_PAGAMENTO_VALIDAS = Set.of("dinheiro", "cartao_credito", "cartao_debito",
			"pix", "boleto", "convenio");

	private static final double TOLERANCIA_ARREDONDAMENTO = 0.01;

	private final ItemTratamentoRepository itemTratamentoRepository;
	private final PagamentoRepository pagamentoRepository;

	public FinanceiroResumoDTO resumo(final int mes, final int ano) {
		final LocalDate inicio = LocalDate.of(ano, mes, 1);
		final LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

		final double totalOrcado = this.itemTratamentoRepository.somarValorNaoCancelado();
		final double totalRecebido = this.pagamentoRepository.somarTodos();
		final double totalPendente = totalOrcado - totalRecebido;
		final double recebidoNoMes = this.pagamentoRepository.somarPorPeriodo(inicio, fim);

		return new FinanceiroResumoDTO(totalOrcado, totalRecebido, totalPendente, recebidoNoMes);
	}

	public List<PagamentoDTO> listarPagamentos(final int mes, final int ano) {
		final LocalDate inicio = LocalDate.of(ano, mes, 1);
		final LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

		return this.pagamentoRepository.listarPorPeriodo(inicio, fim).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	public List<PendenciaTratamentoDTO> listarPendencias() {
		return this.itemTratamentoRepository.listarNaoCanceladosComPaciente().stream()
				.map(item -> {
					final double valorPago = item.getValorPago() != null ? item.getValorPago() : 0.0;
					final double saldo = item.getValor() - valorPago;
					return saldo > 0 ? this.toPendenciaDto(item, valorPago, saldo) : null;
				})
				.filter(pendencia -> pendencia != null)
				.collect(Collectors.toList());
	}

	@Transactional
	public PagamentoDTO registrarPagamento(final PagamentoDTO dto) {
		if (dto.getItemTratamentoId() == null) {
			throw new IllegalArgumentException("itemTratamentoId é obrigatório");
		}

		final ItemTratamento item = this.itemTratamentoRepository.findById(dto.getItemTratamentoId())
				.orElseThrow(() -> new NotFoundException(
						"Item de tratamento com id: " + dto.getItemTratamentoId() + " não encontrado!"));

		this.validarFormaPagamento(dto.getFormaPagamento());

		if (dto.getValor() == null || dto.getValor() <= 0) {
			throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero");
		}

		final double jaPago = item.getValorPago() != null ? item.getValorPago() : 0.0;
		final double saldo = item.getValor() - jaPago;

		if (dto.getValor() > saldo + TOLERANCIA_ARREDONDAMENTO) {
			throw new IllegalArgumentException(
					String.format("Valor do pagamento (%.2f) excede o saldo pendente (%.2f)", dto.getValor(), saldo));
		}

		final Pagamento pagamento = new Pagamento();
		pagamento.setItemTratamento(item);
		pagamento.setValor(dto.getValor());
		pagamento.setFormaPagamento(dto.getFormaPagamento());
		pagamento.setData(dto.getData());
		pagamento.setObservacao(dto.getObservacao());

		final Pagamento salvo = this.pagamentoRepository.save(pagamento);

		item.setValorPago(jaPago + dto.getValor());
		this.itemTratamentoRepository.save(item);

		return this.toDto(salvo);
	}

	// Gera um PDF com o resumo financeiro + lançamentos do mês/ano informado
	public byte[] gerarRelatorioPdf(final int mes, final int ano) {
		final FinanceiroResumoDTO resumo = this.resumo(mes, ano);
		final List<PagamentoDTO> pagamentos = this.listarPagamentos(mes, ano);
		final String mesAno = String.format("%02d/%d", mes, ano);

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			final Document document = PdfReportUtil.novoDocumento(out);
			document.add(PdfReportUtil.titulo("Relatório Financeiro - " + mesAno));
			document.add(new Paragraph(" "));

			document.add(new Paragraph(String.format(Locale.of("pt", "BR"), "Total orçado: R$ %.2f", resumo.getTotalOrcado())));
			document.add(new Paragraph(String.format(Locale.of("pt", "BR"), "Total recebido (histórico): R$ %.2f", resumo.getTotalRecebido())));
			document.add(new Paragraph(String.format(Locale.of("pt", "BR"), "Total pendente: R$ %.2f", resumo.getTotalPendente())));
			document.add(new Paragraph(String.format(Locale.of("pt", "BR"), "Recebido no mês: R$ %.2f", resumo.getRecebidoNoMes())));
			document.add(new Paragraph(" "));
			document.add(PdfReportUtil.subtitulo("Lançamentos do período"));
			document.add(new Paragraph(" "));

			final PdfPTable table = PdfReportUtil.novaTabela(new float[] { 2.5f, 3f, 1.5f, 2f, 1.5f }, "Paciente",
					"Procedimento", "Valor", "Forma", "Data");

			for (final PagamentoDTO pagamento : pagamentos) {
				PdfReportUtil.celula(table, pagamento.getNomePaciente());
				PdfReportUtil.celula(table, pagamento.getProcedimento());
				PdfReportUtil.celula(table, String.format(Locale.of("pt", "BR"), "R$ %.2f", pagamento.getValor()));
				PdfReportUtil.celula(table, pagamento.getFormaPagamento());
				PdfReportUtil.celula(table,
						pagamento.getData() != null ? pagamento.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
			}

			document.add(table);
			document.close();
			return out.toByteArray();
		} catch (final DocumentException | IOException e) {
			throw new RuntimeException("Erro ao gerar relatório financeiro", e);
		}
	}

	private void validarFormaPagamento(final String formaPagamento) {
		if (formaPagamento == null || !FORMAS_PAGAMENTO_VALIDAS.contains(formaPagamento)) {
			throw new IllegalArgumentException(
					"Forma de pagamento inválida. Use: dinheiro, cartao_credito, cartao_debito, pix, boleto ou convenio");
		}
	}

	private PagamentoDTO toDto(final Pagamento pagamento) {
		final ItemTratamento item = pagamento.getItemTratamento();
		return PagamentoDTO.builder().id(pagamento.getId()).itemTratamentoId(item.getId())
				.pacienteId(item.getPaciente().getId()).nomePaciente(item.getPaciente().getNome())
				.procedimento(item.getProcedimento()).valor(pagamento.getValor())
				.formaPagamento(pagamento.getFormaPagamento()).data(pagamento.getData())
				.observacao(pagamento.getObservacao()).criadoEm(pagamento.getCriadoEm()).build();
	}

	private PendenciaTratamentoDTO toPendenciaDto(final ItemTratamento item, final double valorPago,
			final double saldo) {
		return PendenciaTratamentoDTO.builder().itemTratamentoId(item.getId())
				.pacienteId(item.getPaciente().getId()).nomePaciente(item.getPaciente().getNome())
				.procedimento(item.getProcedimento()).valor(item.getValor()).valorPago(valorPago).saldo(saldo)
				.build();
	}
}
