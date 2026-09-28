package com.msp.odonto_pro.service.estoque;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msp.odonto_pro.dto.estoque.ItemEstoqueDTO;
import com.msp.odonto_pro.dto.estoque.MovimentacaoEstoqueDTO;
import com.msp.odonto_pro.entity.estoque.ItemEstoque;
import com.msp.odonto_pro.entity.estoque.MovimentacaoEstoque;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.estoque.ItemEstoqueRepository;
import com.msp.odonto_pro.repository.estoque.MovimentacaoEstoqueRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstoqueService {

	private static final Set<String> TIPOS_MOVIMENTACAO_VALIDOS = Set.of("entrada", "saida");

	private final ItemEstoqueRepository itemEstoqueRepository;
	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	// ===== ITENS =====

	public List<ItemEstoqueDTO> listarItens() {
		return this.itemEstoqueRepository.findByClinicaId(SecurityUtils.clinicaIdAtual()).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	public ItemEstoqueDTO criarItem(final ItemEstoqueDTO dto) {
		this.validarItem(dto);

		final ItemEstoque item = ItemEstoque.builder().clinicaId(SecurityUtils.clinicaIdAtual()).nome(dto.getNome())
				.categoria(dto.getCategoria())
				.unidadeMedida(dto.getUnidadeMedida()).quantidadeAtual(dto.getQuantidadeAtual())
				.quantidadeMinima(dto.getQuantidadeMinima()).fornecedor(dto.getFornecedor())
				.validade(dto.getValidade()).precoUnitario(dto.getPrecoUnitario()).build();

		return this.toDto(this.itemEstoqueRepository.save(item));
	}

	public ItemEstoqueDTO atualizarItem(final Long id, final ItemEstoqueDTO dto) {
		this.validarItem(dto);

		final ItemEstoque item = this.exigirItem(id);
		item.setNome(dto.getNome());
		item.setCategoria(dto.getCategoria());
		item.setUnidadeMedida(dto.getUnidadeMedida());
		item.setQuantidadeAtual(dto.getQuantidadeAtual());
		item.setQuantidadeMinima(dto.getQuantidadeMinima());
		item.setFornecedor(dto.getFornecedor());
		item.setValidade(dto.getValidade());
		item.setPrecoUnitario(dto.getPrecoUnitario());

		return this.toDto(this.itemEstoqueRepository.save(item));
	}

	public void excluirItem(final Long id) {
		this.exigirItem(id);
		this.itemEstoqueRepository.deleteById(id);
	}

	// ===== MOVIMENTAÇÕES =====

	public List<MovimentacaoEstoqueDTO> listarMovimentacoes() {
		return this.movimentacaoEstoqueRepository.listarTodasComItem(SecurityUtils.clinicaIdAtual()).stream()
				.map(this::toDto).collect(Collectors.toList());
	}

	@Transactional
	public MovimentacaoEstoqueDTO registrarMovimentacao(final MovimentacaoEstoqueDTO dto, final Usuario usuarioLogado) {
		if (dto.getItemEstoqueId() == null) {
			throw new IllegalArgumentException("itemEstoqueId é obrigatório");
		}
		if (dto.getTipo() == null || !TIPOS_MOVIMENTACAO_VALIDOS.contains(dto.getTipo())) {
			throw new IllegalArgumentException("Tipo inválido. Use: entrada ou saida");
		}
		if (dto.getQuantidade() == null || dto.getQuantidade() <= 0) {
			throw new IllegalArgumentException("Quantidade deve ser maior que zero");
		}

		final ItemEstoque item = this.exigirItem(dto.getItemEstoqueId());

		if ("saida".equals(dto.getTipo()) && dto.getQuantidade() > item.getQuantidadeAtual()) {
			throw new IllegalArgumentException("Quantidade maior que o estoque disponível");
		}

		final double novaQuantidade = "entrada".equals(dto.getTipo())
				? item.getQuantidadeAtual() + dto.getQuantidade()
				: item.getQuantidadeAtual() - dto.getQuantidade();
		item.setQuantidadeAtual(novaQuantidade);
		this.itemEstoqueRepository.save(item);

		final MovimentacaoEstoque movimentacao = MovimentacaoEstoque.builder().itemEstoque(item).tipo(dto.getTipo())
				.quantidade(dto.getQuantidade()).motivo(dto.getMotivo())
				.criadoPor(usuarioLogado != null ? usuarioLogado.getNome() : null).build();

		return this.toDto(this.movimentacaoEstoqueRepository.save(movimentacao));
	}

	private ItemEstoque exigirItem(final Long id) {
		final ItemEstoque item = this.itemEstoqueRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Item de estoque com id: " + id + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(item.getClinicaId())) {
			throw new NotFoundException("Item de estoque com id: " + id + " não encontrado!");
		}
		return item;
	}

	private void validarItem(final ItemEstoqueDTO dto) {
		if (dto.getNome() == null || dto.getNome().isBlank()) {
			throw new IllegalArgumentException("nome é obrigatório");
		}
		if (dto.getCategoria() == null || dto.getCategoria().isBlank()) {
			throw new IllegalArgumentException("categoria é obrigatória");
		}
		if (dto.getQuantidadeAtual() == null || dto.getQuantidadeAtual() < 0) {
			throw new IllegalArgumentException("quantidadeAtual não pode ser negativa");
		}
		if (dto.getQuantidadeMinima() == null || dto.getQuantidadeMinima() < 0) {
			throw new IllegalArgumentException("quantidadeMinima não pode ser negativa");
		}
	}

	private ItemEstoqueDTO toDto(final ItemEstoque item) {
		return ItemEstoqueDTO.builder().id(item.getId()).nome(item.getNome()).categoria(item.getCategoria())
				.unidadeMedida(item.getUnidadeMedida()).quantidadeAtual(item.getQuantidadeAtual())
				.quantidadeMinima(item.getQuantidadeMinima()).fornecedor(item.getFornecedor())
				.validade(item.getValidade()).precoUnitario(item.getPrecoUnitario())
				.atualizadoEm(item.getAtualizadoEm()).build();
	}

	private MovimentacaoEstoqueDTO toDto(final MovimentacaoEstoque movimentacao) {
		return MovimentacaoEstoqueDTO.builder().id(movimentacao.getId())
				.itemEstoqueId(movimentacao.getItemEstoque().getId())
				.nomeItem(movimentacao.getItemEstoque().getNome()).tipo(movimentacao.getTipo())
				.quantidade(movimentacao.getQuantidade()).motivo(movimentacao.getMotivo())
				.data(movimentacao.getData()).criadoPor(movimentacao.getCriadoPor()).build();
	}
}
