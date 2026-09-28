package com.msp.odonto_pro.service.auditoria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.auditoria.AuditoriaAcessoDTO;
import com.msp.odonto_pro.dto.shared.PageResponseDTO;
import com.msp.odonto_pro.entity.auditoria.AuditoriaAcesso;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.repository.auditoria.AuditoriaAcessoRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Auditoria (LGPD) das ações sensíveis sobre dados de paciente: visualizar
 * prontuário completo, exportar dados, excluir/anonimizar dados. Ver
 * PacienteClinicoController, que chama {@link #registrar} logo após cada
 * uma dessas 3 operações ter sucesso.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaAcessoService {

	public static final String VISUALIZOU_PRONTUARIO = "visualizou_prontuario";
	public static final String EXPORTOU_DADOS = "exportou_dados";
	public static final String EXCLUIU_DADOS = "excluiu_dados";

	private final AuditoriaAcessoRepository auditoriaAcessoRepository;

	public void registrar(final Long pacienteId, final String pacienteNome, final String acao,
			final Usuario usuarioLogado, final String ip) {
		final AuditoriaAcesso registro = AuditoriaAcesso.builder().clinicaId(SecurityUtils.clinicaIdAtual())
				.usuarioId(usuarioLogado.getId()).usuarioNome(usuarioLogado.getNome()).pacienteId(pacienteId)
				.pacienteNome(pacienteNome).acao(acao).ip(ip).build();
		this.auditoriaAcessoRepository.save(registro);
	}

	public PageResponseDTO<AuditoriaAcessoDTO> listar(final Integer pagina, final Integer tamanho, final String busca,
			final LocalDate dataInicio, final LocalDate dataFim) {
		final int paginaSegura = pagina != null && pagina > 0 ? pagina - 1 : 0;
		final int tamanhoSeguro = tamanho != null && tamanho > 0 ? tamanho : 20;

		final Page<AuditoriaAcesso> page = this.auditoriaAcessoRepository.findAll(
				this.montarFiltro(busca, dataInicio, dataFim), PageRequest.of(paginaSegura, tamanhoSeguro));

		return new PageResponseDTO<>(page.getContent().stream().map(this::toDto).toList(), page.getTotalElements(),
				page.getTotalPages(), page.getNumber());
	}

	/**
	 * Monta os predicados dinamicamente em Java (Specification), só incluindo
	 * cada filtro quando ele realmente veio preenchido — evita mandar pro
	 * Postgres um bind "? IS NULL" ambíguo (JPQL condicional com parâmetro
	 * null trava com "could not determine data type of parameter" / "function
	 * lower(bytea) does not exist" nesse driver+versão do Hibernate).
	 */
	private Specification<AuditoriaAcesso> montarFiltro(final String busca, final LocalDate dataInicio,
			final LocalDate dataFim) {
		final Long clinicaId = SecurityUtils.clinicaIdAtual();

		return (root, query, cb) -> {
			final List<Predicate> predicados = new ArrayList<>();
			predicados.add(cb.equal(root.get("clinicaId"), clinicaId));

			if (busca != null && !busca.isBlank()) {
				final String padrao = "%" + busca.trim().toLowerCase() + "%";
				predicados.add(cb.or(cb.like(cb.lower(root.get("usuarioNome")), padrao),
						cb.like(cb.lower(root.get("pacienteNome")), padrao)));
			}
			if (dataInicio != null) {
				predicados.add(cb.greaterThanOrEqualTo(root.get("criadoEm"), dataInicio.atStartOfDay()));
			}
			if (dataFim != null) {
				predicados.add(cb.lessThanOrEqualTo(root.get("criadoEm"), LocalDateTime.of(dataFim, LocalTime.MAX)));
			}

			query.orderBy(cb.desc(root.get("criadoEm")));
			return cb.and(predicados.toArray(new Predicate[0]));
		};
	}

	private AuditoriaAcessoDTO toDto(final AuditoriaAcesso registro) {
		return AuditoriaAcessoDTO.builder().id(registro.getId()).usuarioId(registro.getUsuarioId())
				.usuarioNome(registro.getUsuarioNome()).pacienteId(registro.getPacienteId())
				.pacienteNome(registro.getPacienteNome()).acao(registro.getAcao()).criadoEm(registro.getCriadoEm())
				.ip(registro.getIp()).build();
	}
}
