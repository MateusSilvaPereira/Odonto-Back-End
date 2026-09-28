package com.msp.odonto_pro.service.financeiro;

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
import com.msp.odonto_pro.dto.financeiro.DespesaDTO;
import com.msp.odonto_pro.dto.financeiro.FinanceiroResumoDTO;
import com.msp.odonto_pro.dto.financeiro.PagamentoDTO;
import com.msp.odonto_pro.dto.financeiro.PendenciaTratamentoDTO;
import com.msp.odonto_pro.entity.financeiro.Despesa;
import com.msp.odonto_pro.entity.itemtratamento.ItemTratamento;
import com.msp.odonto_pro.entity.financeiro.Pagamento;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.financeiro.DespesaRepository;
import com.msp.odonto_pro.repository.itemtratamento.ItemTratamentoRepository;
import com.msp.odonto_pro.repository.financeiro.PagamentoRepository;
import com.msp.odonto_pro.security.SecurityUtils;
import com.msp.odonto_pro.util.PdfReportUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinanceiroService {

	private static final Set<String> FORMAS_PAGAMENTO_VALIDAS = Set.of("dinheiro", "cartao_credito", "cartao_debito",
			"pix", "boleto", "convenio", "parcelamento");

	private static final Set<String> CATEGORIAS_DESPESA_VALIDAS = Set.of("material_odontologico", "aluguel",
			"salarios", "equipamentos", "marketing", "impostos", "outros");

	private static final double TOLERANCIA_ARREDONDAMENTO = 0.01;

	private final ItemTratamentoRepository itemTratamentoRepository;
	private final PagamentoRepository pagamentoRepository;
	private final DespesaRepository despesaRepository;

	public FinanceiroResumoDTO resumo(final int mes, final int ano) {
		final Long clinicaId = SecurityUtils.clinicaIdAtual();
		final LocalDate inicio = LocalDate.of(ano, mes, 1);
		final LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

		final double totalOrcado = this.itemTratamentoRepository.somarValorNaoCancelado(clinicaId);
		final double totalRecebido = this.pagamentoRepository.somarTodos(clinicaId);
		final double totalPendente = totalOrcado - totalRecebido;
		final double recebidoNoMes = this.pagamentoRepository.somarPorPeriodo(clinicaId, inicio, fim);

		return new FinanceiroResumoDTO(totalOrcado, totalRecebido, totalPendente, recebidoNoMes);
	}

	/**
	 * @param mes        opcional: quando ausente, o período vira o ano inteiro
	 *                   (usado pelo informe de rendimentos/IR)
	 * @param pacienteId opcional: filtra só os pagamentos deste paciente
	 */
	public List<PagamentoDTO> listarPagamentos(final Integer mes, final int ano, final Long pacienteId) {
		final LocalDate inicio = mes != null ? LocalDate.of(ano, mes, 1) : LocalDate.of(ano, 1, 1);
		final LocalDate fim = mes != null ? inicio.withDayOfMonth(inicio.lengthOfMonth()) : LocalDate.of(ano, 12, 31);

		return this.pagamentoRepository.listarPorPeriodo(SecurityUtils.clinicaIdAtual(), inicio, fim, pacienteId)
				.stream().map(this::toDto).collect(Collectors.toList());
	}

	public List<PendenciaTratamentoDTO> listarPendencias() {
		return this.itemTratamentoRepository.listarNaoCanceladosComPaciente(SecurityUtils.clinicaIdAtual()).stream()
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
		if (!SecurityUtils.clinicaIdAtual().equals(item.getPaciente().getClinicaId())) {
			throw new NotFoundException(
					"Item de tratamento com id: " + dto.getItemTratamentoId() + " não encontrado!");
		}

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
		final List<PagamentoDTO> pagamentos = this.listarPagamentos(mes, ano, null);
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

	// ===== CONTAS A PAGAR (DESPESAS) =====

	public List<DespesaDTO> listarDespesas(final int mes, final int ano) {
		final LocalDate inicio = LocalDate.of(ano, mes, 1);
		final LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

		return this.despesaRepository.listarPorPeriodo(SecurityUtils.clinicaIdAtual(), inicio, fim).stream()
				.map(this::toDto).collect(Collectors.toList());
	}

	public DespesaDTO criarDespesa(final DespesaDTO dto) {
		if (dto.getDescricao() == null || dto.getDescricao().isBlank()) {
			throw new IllegalArgumentException("descricao é obrigatória");
		}
		if (dto.getValor() == null || dto.getValor() <= 0) {
			throw new IllegalArgumentException("Valor da despesa deve ser maior que zero");
		}
		if (dto.getDataVencimento() == null) {
			throw new IllegalArgumentException("dataVencimento é obrigatória");
		}
		this.validarCategoriaDespesa(dto.getCategoria());

		final Despesa despesa = Despesa.builder().clinicaId(SecurityUtils.clinicaIdAtual())
				.descricao(dto.getDescricao()).categoria(dto.getCategoria())
				.valor(dto.getValor()).dataVencimento(dto.getDataVencimento()).fornecedor(dto.getFornecedor())
				.observacao(dto.getObservacao()).status("pendente").build();

		return this.toDto(this.despesaRepository.save(despesa));
	}

	@Transactional
	public DespesaDTO marcarComoPaga(final Long id) {
		final Despesa despesa = this.exigirDespesa(id);

		despesa.setStatus("pago");
		despesa.setDataPagamento(LocalDate.now());

		return this.toDto(this.despesaRepository.save(despesa));
	}

	public void excluirDespesa(final Long id) {
		this.exigirDespesa(id);
		this.despesaRepository.deleteById(id);
	}

	private Despesa exigirDespesa(final Long id) {
		final Despesa despesa = this.despesaRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Despesa com id: " + id + " não encontrada!"));
		if (!SecurityUtils.clinicaIdAtual().equals(despesa.getClinicaId())) {
			throw new NotFoundException("Despesa com id: " + id + " não encontrada!");
		}
		return despesa;
	}

	private void validarCategoriaDespesa(final String categoria) {
		if (categoria == null || !CATEGORIAS_DESPESA_VALIDAS.contains(categoria)) {
			throw new IllegalArgumentException(
					"Categoria inválida. Use: material_odontologico, aluguel, salarios, equipamentos, marketing, impostos ou outros");
		}
	}

	private DespesaDTO toDto(final Despesa despesa) {
		// "atrasado" é calculado na leitura (não persistido) para despesas
		// pendentes cujo vencimento já passou.
		final boolean atrasada = "pendente".equals(despesa.getStatus())
				&& despesa.getDataVencimento().isBefore(LocalDate.now());

		return DespesaDTO.builder().id(despesa.getId()).descricao(despesa.getDescricao())
				.categoria(despesa.getCategoria()).valor(despesa.getValor())
				.dataVencimento(despesa.getDataVencimento()).dataPagamento(despesa.getDataPagamento())
				.status(atrasada ? "atrasado" : despesa.getStatus()).fornecedor(despesa.getFornecedor())
				.observacao(despesa.getObservacao()).criadoEm(despesa.getCriadoEm()).build();
	}

	private void validarFormaPagamento(final String formaPagamento) {
		if (formaPagamento == null || !FORMAS_PAGAMENTO_VALIDAS.contains(formaPagamento)) {
			throw new IllegalArgumentException(
					"Forma de pagamento inválida. Use: dinheiro, cartao_credito, cartao_debito, pix, boleto, convenio ou parcelamento");
		}
	}

	private PagamentoDTO toDto(final Pagamento pagamento) {
		final ItemTratamento item = pagamento.getItemTratamento();
		return PagamentoDTO.builder().id(pagamento.getId()).itemTratamentoId(item.getId())
				.pacienteId(item.getPaciente().getId()).nomePaciente(item.getPaciente().getNome())
				.procedimento(item.getProcedimento())
				.dentistaId(item.getDentista() != null ? item.getDentista().getId() : null)
				.dentista(item.getNomeProfissional()).valor(pagamento.getValor())
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
