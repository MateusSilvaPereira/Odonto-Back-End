package com.msp.odonto_pro.service.orcamento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msp.odonto_pro.dto.orcamento.OrcamentoPublicoDTO;
import com.msp.odonto_pro.dto.orcamento.OrcamentoPublicoItemDTO;
import com.msp.odonto_pro.entity.clinica.Clinica;
import com.msp.odonto_pro.entity.itemtratamento.ItemTratamento;
import com.msp.odonto_pro.entity.orcamento.Orcamento;
import com.msp.odonto_pro.entity.orcamento.OrcamentoItem;
import com.msp.odonto_pro.entity.parcela.Parcela;
import com.msp.odonto_pro.exceptions.LinkExpiradoException;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.clinica.ClinicaRepository;
import com.msp.odonto_pro.repository.itemtratamento.ItemTratamentoRepository;
import com.msp.odonto_pro.repository.orcamento.OrcamentoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Serviço do fluxo público (sem autenticação) de aprovação/recusa de
 * orçamento pelo próprio paciente, via link. Não reusa OrcamentoService de
 * propósito — mesmo motivo de ConsultaPublicaService: nunca deve expor ou
 * aceitar nada que dependa de usuário autenticado, nem do isolamento por
 * clínica (o linkToken em si já é a autorização, igual ao tokenConfirmacao
 * da Consulta).
 */
@Service
@RequiredArgsConstructor
public class OrcamentoPublicoService {

	private static final String STATUS_PENDENTE = "pendente";
	private static final String STATUS_APROVADO = "aprovado";
	private static final String STATUS_RECUSADO = "recusado";
	private static final String STATUS_EXPIRADO = "expirado";

	private final OrcamentoRepository orcamentoRepository;
	private final ItemTratamentoRepository itemTratamentoRepository;
	private final ClinicaRepository clinicaRepository;

	public OrcamentoPublicoDTO buscarPorToken(final String token) {
		return this.toDto(this.buscarOrcamentoOuFalhar(token));
	}

	/** Mesmo efeito de OrcamentoService.aprovar (gera ItemTratamento por item + parcelas), duplicado de propósito. */
	@Transactional
	public OrcamentoPublicoDTO aprovarPorToken(final String token) {
		final Orcamento orcamento = this.buscarOrcamentoOuFalhar(token);
		this.garantirRespondivel(orcamento);

		orcamento.setStatus(STATUS_APROVADO);
		orcamento.setRespondidoEm(LocalDateTime.now());

		for (final OrcamentoItem item : orcamento.getItens()) {
			final ItemTratamento novoItemTratamento = new ItemTratamento();
			novoItemTratamento.setPaciente(orcamento.getPaciente());
			novoItemTratamento.setDente(item.getDente());
			novoItemTratamento.setProcedimento(item.getProcedimento());
			novoItemTratamento.setValor(item.getValor() - (item.getDesconto() != null ? item.getDesconto() : 0.0));
			novoItemTratamento.setStatus("aprovado");
			novoItemTratamento.setOrcamentoId(orcamento.getId());
			this.itemTratamentoRepository.save(novoItemTratamento);
		}

		orcamento.getParcelas().clear();
		orcamento.getParcelas().addAll(this.gerarParcelas(orcamento));

		return this.toDto(this.orcamentoRepository.save(orcamento));
	}

	@Transactional
	public OrcamentoPublicoDTO recusarPorToken(final String token) {
		final Orcamento orcamento = this.buscarOrcamentoOuFalhar(token);
		this.garantirRespondivel(orcamento);

		orcamento.setStatus(STATUS_RECUSADO);
		orcamento.setRespondidoEm(LocalDateTime.now());

		return this.toDto(this.orcamentoRepository.save(orcamento));
	}

	/** Mesma fórmula de OrcamentoService.gerarParcelas, duplicada de propósito (ver comentário da classe). */
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

	private void garantirRespondivel(final Orcamento orcamento) {
		if (!STATUS_PENDENTE.equals(orcamento.getStatus())) {
			throw new LinkExpiradoException("Este orçamento já foi respondido");
		}
		if (orcamento.getValidade().isBefore(LocalDate.now())) {
			throw new LinkExpiradoException("Este orçamento está expirado e não pode mais ser respondido");
		}
	}

	private Orcamento buscarOrcamentoOuFalhar(final String token) {
		return this.orcamentoRepository.findByLinkToken(token)
				.orElseThrow(() -> new NotFoundException("Link inválido ou orçamento não encontrado"));
	}

	private double calcularValorTotal(final Orcamento orcamento) {
		return orcamento.getItens().stream()
				.mapToDouble(item -> item.getValor() - (item.getDesconto() != null ? item.getDesconto() : 0.0)).sum();
	}

	private String calcularStatusExibido(final Orcamento orcamento) {
		if (STATUS_PENDENTE.equals(orcamento.getStatus()) && orcamento.getValidade().isBefore(LocalDate.now())) {
			return STATUS_EXPIRADO;
		}
		return orcamento.getStatus();
	}

	private String resolverNomeClinica(final Orcamento orcamento) {
		final Long clinicaId = orcamento.getPaciente().getClinicaId();
		if (clinicaId == null) {
			return null;
		}
		return this.clinicaRepository.findById(clinicaId).map(Clinica::getNome).orElse(null);
	}

	private OrcamentoPublicoDTO toDto(final Orcamento orcamento) {
		final String statusExibido = this.calcularStatusExibido(orcamento);
		return OrcamentoPublicoDTO.builder().nomePaciente(orcamento.getPaciente().getNome())
				.nomeClinica(this.resolverNomeClinica(orcamento))
				.itens(orcamento.getItens().stream().map(this::toItemDto).collect(Collectors.toList()))
				.valorTotal(this.calcularValorTotal(orcamento)).condicaoPagamento(orcamento.getCondicaoPagamento())
				.numeroParcelas(orcamento.getNumeroParcelas()).validade(orcamento.getValidade())
				.status(statusExibido).linkExpirado(STATUS_EXPIRADO.equals(statusExibido)).build();
	}

	private OrcamentoPublicoItemDTO toItemDto(final OrcamentoItem item) {
		return OrcamentoPublicoItemDTO.builder().dente(item.getDente()).procedimento(item.getProcedimento())
				.valor(item.getValor()).desconto(item.getDesconto()).build();
	}
}
