package com.msp.odonto_pro.controller.financeiro;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.financeiro.DespesaDTO;
import com.msp.odonto_pro.dto.financeiro.FinanceiroResumoDTO;
import com.msp.odonto_pro.dto.financeiro.PagamentoDTO;
import com.msp.odonto_pro.dto.financeiro.PendenciaTratamentoDTO;
import com.msp.odonto_pro.service.financeiro.FinanceiroService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.V1 + "/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

	private final FinanceiroService financeiroService;

	@PreAuthorize("hasAuthority('read:financeiro')")
	@Operation(summary = "Resumo financeiro do mês/ano informado.")
	@GetMapping("/resumo")
	public ResponseEntity<FinanceiroResumoDTO> resumo(@RequestParam final int mes, @RequestParam final int ano) {
		return ResponseEntity.ok(this.financeiroService.resumo(mes, ano));
	}

	@PreAuthorize("hasAuthority('read:financeiro')")
	@Operation(summary = "Pagamentos registrados no período informado. "
			+ "Sem \"mes\", retorna o ano inteiro (usado no informe de rendimentos/IR). "
			+ "Com \"pacienteId\", filtra só os pagamentos desse paciente.")
	@GetMapping("/pagamentos")
	public ResponseEntity<List<PagamentoDTO>> pagamentos(@RequestParam(required = false) final Integer mes,
			@RequestParam final int ano, @RequestParam(required = false) final Long pacienteId) {
		return ResponseEntity.ok(this.financeiroService.listarPagamentos(mes, ano, pacienteId));
	}

	@PreAuthorize("hasAuthority('read:financeiro')")
	@Operation(summary = "Itens de tratamento com saldo pendente, de todos os pacientes.")
	@GetMapping("/pendencias")
	public ResponseEntity<List<PendenciaTratamentoDTO>> pendencias() {
		return ResponseEntity.ok(this.financeiroService.listarPendencias());
	}

	@PreAuthorize("hasAuthority('read:financeiro')")
	@Operation(summary = "Gerar relatório financeiro do mês/ano em PDF.")
	@GetMapping("/relatorio")
	public ResponseEntity<byte[]> relatorio(@RequestParam final int mes, @RequestParam final int ano) {
		final byte[] pdf = this.financeiroService.gerarRelatorioPdf(mes, ano);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-financeiro.pdf").body(pdf);
	}

	@PreAuthorize("hasAuthority('read:financeiro')")
	@Operation(summary = "Despesas (contas a pagar) com vencimento no mês/ano informado.")
	@GetMapping("/despesas")
	public ResponseEntity<List<DespesaDTO>> despesas(@RequestParam final int mes, @RequestParam final int ano) {
		return ResponseEntity.ok(this.financeiroService.listarDespesas(mes, ano));
	}

	@PreAuthorize("hasAuthority('write:financeiro')")
	@Operation(summary = "Registrar nova despesa (conta a pagar).")
	@PostMapping("/despesas")
	public ResponseEntity<DespesaDTO> criarDespesa(@RequestBody final DespesaDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.financeiroService.criarDespesa(dto));
	}

	@PreAuthorize("hasAuthority('write:financeiro')")
	@Operation(summary = "Marcar despesa como paga.")
	@PatchMapping("/despesas/{id}/pagar")
	public ResponseEntity<DespesaDTO> pagarDespesa(@PathVariable final Long id) {
		return ResponseEntity.ok(this.financeiroService.marcarComoPaga(id));
	}

	@PreAuthorize("hasAuthority('write:financeiro')")
	@Operation(summary = "Excluir despesa por id.")
	@DeleteMapping("/despesas/{id}")
	public ResponseEntity<Void> excluirDespesa(@PathVariable final Long id) {
		this.financeiroService.excluirDespesa(id);
		return ResponseEntity.noContent().build();
	}
}
