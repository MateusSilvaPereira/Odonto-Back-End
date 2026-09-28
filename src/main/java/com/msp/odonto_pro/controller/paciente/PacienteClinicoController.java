package com.msp.odonto_pro.controller.paciente;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dto.odontograma.DenteRegistroDTO;
import com.msp.odonto_pro.dto.evolucaoclinica.EvolucaoClinicaDTO;
import com.msp.odonto_pro.dto.itemtratamento.ItemTratamentoDTO;
import com.msp.odonto_pro.dto.orcamento.OrcamentoDTO;
import com.msp.odonto_pro.dto.prontuario.ProntuarioDTO;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.service.auditoria.AuditoriaAcessoService;
import com.msp.odonto_pro.service.evolucaoclinica.EvolucaoClinicaService;
import com.msp.odonto_pro.service.itemtratamento.ItemTratamentoService;
import com.msp.odonto_pro.service.odontograma.OdontogramaService;
import com.msp.odonto_pro.service.orcamento.OrcamentoService;
import com.msp.odonto_pro.service.paciente.PacienteLgpdService;
import com.msp.odonto_pro.service.paciente.PacienteService;
import com.msp.odonto_pro.service.prontuario.ProntuarioService;
import com.msp.odonto_pro.web.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Sub-recursos clínicos de um paciente: prontuário, evolução clínica,
 * odontograma e plano de tratamento. Classe separada de PacienteController
 * (CRUD básico) para manter responsabilidade única por controller.
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/pacientes")
@RequiredArgsConstructor
public class PacienteClinicoController {

	private final OdontogramaService odontogramaService;
	private final ItemTratamentoService itemTratamentoService;
	private final ProntuarioService prontuarioService;
	private final EvolucaoClinicaService evolucaoClinicaService;
	private final OrcamentoService orcamentoService;
	private final PacienteLgpdService pacienteLgpdService;
	private final PacienteService pacienteService;
	private final AuditoriaAcessoService auditoriaAcessoService;

	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Obter odontograma do paciente.")
	@GetMapping("/{id}/odontograma")
	public ResponseEntity<OdontogramaResponse> obterOdontograma(@PathVariable final Long id) {
		return ResponseEntity.ok(new OdontogramaResponse(this.odontogramaService.listar(id)));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Atualizar status/observação de um dente do odontograma.")
	@PutMapping("/{id}/odontograma/dente/{numero}")
	public ResponseEntity<DenteRegistroDTO> atualizarDente(@PathVariable final Long id,
			@PathVariable final String numero, @RequestBody final DenteRegistroDTO dto) {
		return ResponseEntity.ok(this.odontogramaService.atualizarDente(id, numero, dto));
	}

	@PreAuthorize("hasAuthority('read:tratamento')")
	@Operation(summary = "Listar plano de tratamento do paciente.")
	@GetMapping("/{id}/tratamentos")
	public ResponseEntity<List<ItemTratamentoDTO>> listarTratamentos(@PathVariable final Long id) {
		return ResponseEntity.ok(this.itemTratamentoService.listarPorPaciente(id));
	}

	@PreAuthorize("hasAuthority('write:tratamento')")
	@Operation(summary = "Adicionar item ao plano de tratamento do paciente.")
	@PostMapping("/{id}/tratamentos")
	public ResponseEntity<ItemTratamentoDTO> criarTratamento(@PathVariable final Long id,
			@RequestBody final ItemTratamentoDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.itemTratamentoService.criar(id, dto));
	}

	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Obter prontuário (dados de cadastro clínico) do paciente.")
	@GetMapping("/{id}/prontuario")
	public ResponseEntity<ProntuarioDTO> obterProntuario(@PathVariable final Long id,
			final Authentication authentication, final HttpServletRequest request) {
		final ProntuarioDTO dto = this.prontuarioService.obter(id);
		this.registrarAuditoria(id, AuditoriaAcessoService.VISUALIZOU_PRONTUARIO, authentication, request);
		return ResponseEntity.ok(dto);
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Atualizar prontuário do paciente.")
	@PutMapping("/{id}/prontuario")
	public ResponseEntity<ProntuarioDTO> atualizarProntuario(@PathVariable final Long id,
			@RequestBody final ProntuarioDTO dto, final Authentication authentication) {
		final Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
		return ResponseEntity.ok(this.prontuarioService.atualizar(id, dto, usuarioLogado));
	}

	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Listar timeline de evoluções clínicas do paciente.")
	@GetMapping("/{id}/evolucoes")
	public ResponseEntity<List<EvolucaoClinicaDTO>> listarEvolucoes(@PathVariable final Long id) {
		return ResponseEntity.ok(this.evolucaoClinicaService.listar(id));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Registrar nova evolução clínica (append-only, sem edição/exclusão).")
	@PostMapping("/{id}/evolucoes")
	public ResponseEntity<EvolucaoClinicaDTO> criarEvolucao(@PathVariable final Long id,
			@RequestBody final EvolucaoClinicaDTO dto, final Authentication authentication) {
		final Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(this.evolucaoClinicaService.criar(id, dto, usuarioLogado));
	}

	@PreAuthorize("hasAuthority('read:tratamento')")
	@Operation(summary = "Listar orçamentos do paciente.")
	@GetMapping("/{id}/orcamentos")
	public ResponseEntity<List<OrcamentoDTO>> listarOrcamentos(@PathVariable final Long id) {
		return ResponseEntity.ok(this.orcamentoService.listar(id));
	}

	@PreAuthorize("hasAuthority('write:tratamento')")
	@Operation(summary = "Criar orçamento (proposta) para o paciente.")
	@PostMapping("/{id}/orcamentos")
	public ResponseEntity<OrcamentoDTO> criarOrcamento(@PathVariable final Long id,
			@RequestBody final OrcamentoDTO dto, final Authentication authentication) {
		final Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
		return ResponseEntity.status(HttpStatus.CREATED).body(this.orcamentoService.criar(id, dto, usuarioLogado));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Exportar todos os dados do paciente (LGPD).")
	@GetMapping("/{id}/exportar-dados")
	public ResponseEntity<byte[]> exportarDados(@PathVariable final Long id, final Authentication authentication,
			final HttpServletRequest request) throws IOException {
		final byte[] json = this.pacienteLgpdService.exportarDados(id);
		this.registrarAuditoria(id, AuditoriaAcessoService.EXPORTOU_DADOS, authentication, request);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=paciente-" + id + "-dados.json")
				.body(json);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Anonimizar os dados pessoais do paciente (LGPD). Mantém registros clínicos/financeiros vinculados, porém sem identificação direta.")
	@DeleteMapping("/{id}/dados-pessoais")
	public ResponseEntity<Void> excluirDadosPessoais(@PathVariable final Long id, final Authentication authentication,
			final HttpServletRequest request) {
		// Nome do paciente ANTES de anonimizar — depois disso o cadastro já
		// vira "Paciente Anonimizado #id" (ver PacienteLgpdService).
		final String pacienteNome = this.pacienteService.buscarPorId(id).getNome();
		this.pacienteLgpdService.anonimizarDadosPessoais(id);
		this.registrarAuditoria(id, pacienteNome, AuditoriaAcessoService.EXCLUIU_DADOS, authentication, request);
		return ResponseEntity.noContent().build();
	}

	private void registrarAuditoria(final Long pacienteId, final String acao, final Authentication authentication,
			final HttpServletRequest request) {
		final String pacienteNome = this.pacienteService.buscarPorId(pacienteId).getNome();
		this.registrarAuditoria(pacienteId, pacienteNome, acao, authentication, request);
	}

	private void registrarAuditoria(final Long pacienteId, final String pacienteNome, final String acao,
			final Authentication authentication, final HttpServletRequest request) {
		final Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
		this.auditoriaAcessoService.registrar(pacienteId, pacienteNome, acao, usuarioLogado, request.getRemoteAddr());
	}

	@Data
	@AllArgsConstructor
	public static class OdontogramaResponse {
		private List<DenteRegistroDTO> dentes;
	}
}
