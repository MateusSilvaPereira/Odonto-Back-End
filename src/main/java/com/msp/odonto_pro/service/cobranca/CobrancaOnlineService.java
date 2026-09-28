package com.msp.odonto_pro.service.cobranca;
import com.msp.odonto_pro.service.financeiro.FinanceiroService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.cobranca.CobrancaCreateRequest;
import com.msp.odonto_pro.dto.cobranca.CobrancaOnlineDTO;
import com.msp.odonto_pro.dto.financeiro.PagamentoDTO;
import com.msp.odonto_pro.entity.cobranca.CobrancaOnline;
import com.msp.odonto_pro.exceptions.ConflitoException;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.cobranca.CobrancaOnlineRepository;
import com.msp.odonto_pro.repository.itemtratamento.ItemTratamentoRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * Cobrança online (Pix/Boleto). MOCK temporário: nenhuma integração real de
 * pagamento acontece aqui — ver {@link CobrancaOnline} e o comentário no
 * controller sobre {@code simular-pagamento}.
 */
@Service
@RequiredArgsConstructor
public class CobrancaOnlineService {

	private static final Set<String> FORMAS_VALIDAS = Set.of("pix", "boleto");
	private static final String STATUS_PENDENTE = "pendente";
	private static final String STATUS_PAGO = "pago";

	private final CobrancaOnlineRepository cobrancaOnlineRepository;
	private final ItemTratamentoRepository itemTratamentoRepository;
	private final FinanceiroService financeiroService;

	public CobrancaOnlineDTO criar(final CobrancaCreateRequest request) {
		if (request.getItemTratamentoId() == null) {
			throw new IllegalArgumentException("itemTratamentoId é obrigatório");
		}
		if (!this.itemTratamentoRepository.existsByIdAndPaciente_ClinicaId(request.getItemTratamentoId(),
				SecurityUtils.clinicaIdAtual())) {
			throw new NotFoundException(
					"Item de tratamento com id: " + request.getItemTratamentoId() + " não encontrado!");
		}
		if (request.getValor() == null || request.getValor() <= 0) {
			throw new IllegalArgumentException("valor deve ser maior que zero");
		}
		if (request.getForma() == null || !FORMAS_VALIDAS.contains(request.getForma())) {
			throw new IllegalArgumentException("forma deve ser \"pix\" ou \"boleto\"");
		}

		final boolean isPix = "pix".equals(request.getForma());

		final CobrancaOnline cobranca = CobrancaOnline.builder().itemTratamentoId(request.getItemTratamentoId())
				.valor(request.getValor()).forma(request.getForma()).status(STATUS_PENDENTE)
				.qrCode(isPix ? this.gerarQrCodeFake() : null)
				.linhaDigitavel(isPix ? null : this.gerarLinhaDigitavelFake())
				.vencimento(isPix ? null : request.getVencimento()).build();

		return this.toDto(this.cobrancaOnlineRepository.save(cobranca));
	}

	public CobrancaOnlineDTO obter(final Long id) {
		return this.toDto(this.exigir(id));
	}

	public CobrancaOnlineDTO simularPagamento(final Long id) {
		final CobrancaOnline cobranca = this.exigir(id);

		if (STATUS_PAGO.equals(cobranca.getStatus())) {
			throw new ConflitoException("Esta cobrança já está paga");
		}

		cobranca.setStatus(STATUS_PAGO);
		cobranca.setPagoEm(LocalDateTime.now());
		this.cobrancaOnlineRepository.save(cobranca);

		final PagamentoDTO pagamentoDto = PagamentoDTO.builder().itemTratamentoId(cobranca.getItemTratamentoId())
				.valor(cobranca.getValor()).formaPagamento(cobranca.getForma()).data(LocalDate.now())
				.observacao("Pagamento via cobrança online #" + cobranca.getId() + " (mock)").build();
		this.financeiroService.registrarPagamento(pagamentoDto);

		return this.toDto(cobranca);
	}

	private CobrancaOnline exigir(final Long id) {
		final CobrancaOnline cobranca = this.cobrancaOnlineRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Cobrança com id: " + id + " não encontrada!"));
		if (!this.itemTratamentoRepository.existsByIdAndPaciente_ClinicaId(cobranca.getItemTratamentoId(),
				SecurityUtils.clinicaIdAtual())) {
			throw new NotFoundException("Cobrança com id: " + id + " não encontrada!");
		}
		return cobranca;
	}

	private String gerarQrCodeFake() {
		return "00020126MOCKPIX" + UUID.randomUUID().toString().replace("-", "").toUpperCase() + "5204000053039865802BR6304MOCK";
	}

	private String gerarLinhaDigitavelFake() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 47; i++) {
			sb.append((int) (Math.random() * 10));
		}
		return sb.toString();
	}

	private CobrancaOnlineDTO toDto(final CobrancaOnline cobranca) {
		return CobrancaOnlineDTO.builder().id(cobranca.getId()).itemTratamentoId(cobranca.getItemTratamentoId())
				.valor(cobranca.getValor()).forma(cobranca.getForma()).status(cobranca.getStatus())
				.qrCode(cobranca.getQrCode()).linhaDigitavel(cobranca.getLinhaDigitavel())
				.vencimento(cobranca.getVencimento())
				.criadoEm(cobranca.getCriadoEm()).pagoEm(cobranca.getPagoEm()).build();
	}
}
