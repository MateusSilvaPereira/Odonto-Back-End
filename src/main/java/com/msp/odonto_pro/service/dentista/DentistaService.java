package com.msp.odonto_pro.service.dentista;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.msp.odonto_pro.dto.dentista.DentistaDTO;
import com.msp.odonto_pro.dto.dentista.HorarioAtendimentoDTO;
import com.msp.odonto_pro.entity.dentista.Dentista;
import com.msp.odonto_pro.entity.dentista.HorarioAtendimento;
import com.msp.odonto_pro.exceptions.ConflitoException;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.consulta.ConsultaRepository;
import com.msp.odonto_pro.repository.dentista.DentistaRepository;
import com.msp.odonto_pro.security.SecurityUtils;
import com.msp.odonto_pro.util.ArquivoUploadUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DentistaService {

	private static final Pattern HEX_COLOR = Pattern.compile("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");

	private final DentistaRepository dentistaRepository;
	private final ConsultaRepository consultaRepository;

	public List<DentistaDTO> listar() {
		return this.dentistaRepository.findByClinicaId(SecurityUtils.clinicaIdAtual()).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	public DentistaDTO criar(final DentistaDTO dto) {
		this.validar(dto, null);

		final Dentista dentista = Dentista.builder().clinicaId(SecurityUtils.clinicaIdAtual()).nome(dto.getNome().trim())
				.cro(dto.getCro().trim())
				.especialidade(dto.getEspecialidade()).telefone(dto.getTelefone()).email(dto.getEmail())
				.corAgenda(dto.getCorAgenda().trim()).observacoes(dto.getObservacoes())
				.ativo(dto.getAtivo() == null || dto.getAtivo()).horarioAtendimento(this.paraEntidade(dto.getHorarioAtendimento()))
				.percentualComissao(dto.getPercentualComissao() != null ? dto.getPercentualComissao() : 0.0).build();

		return this.toDto(this.salvar(dentista));
	}

	public DentistaDTO atualizar(final Long id, final DentistaDTO dto) {
		this.validar(dto, id);

		final Dentista dentista = this.exigir(id);
		dentista.setNome(dto.getNome().trim());
		dentista.setCro(dto.getCro().trim());
		dentista.setEspecialidade(dto.getEspecialidade());
		dentista.setTelefone(dto.getTelefone());
		dentista.setEmail(dto.getEmail());
		dentista.setCorAgenda(dto.getCorAgenda().trim());
		dentista.setObservacoes(dto.getObservacoes());
		if (dto.getAtivo() != null) {
			dentista.setAtivo(dto.getAtivo());
		}
		dentista.setHorarioAtendimento(this.paraEntidade(dto.getHorarioAtendimento()));
		dentista.setPercentualComissao(dto.getPercentualComissao() != null ? dto.getPercentualComissao() : 0.0);

		return this.toDto(this.salvar(dentista));
	}

	/**
	 * Rede de segurança contra corrida entre a checagem de CRO duplicado (item
	 * anterior) e o INSERT/UPDATE em si: se ainda assim a constraint única do
	 * banco for violada, devolve 409 em vez de deixar estourar um 500 cru.
	 */
	private Dentista salvar(final Dentista dentista) {
		try {
			return this.dentistaRepository.save(dentista);
		} catch (final DataIntegrityViolationException e) {
			throw new ConflitoException("Já existe um dentista cadastrado com este CRO");
		}
	}

	public DentistaDTO atualizarFoto(final Long id, final MultipartFile foto) throws IOException {
		final Dentista dentista = this.exigir(id);
		if (foto == null || foto.isEmpty()) {
			throw new IllegalArgumentException("foto é obrigatória");
		}
		dentista.setFotoUrl(ArquivoUploadUtil.salvar(foto, "fotos-dentistas"));
		return this.toDto(this.dentistaRepository.save(dentista));
	}

	public DentistaDTO removerFoto(final Long id) {
		final Dentista dentista = this.exigir(id);
		dentista.setFotoUrl(null);
		return this.toDto(this.dentistaRepository.save(dentista));
	}

	public DentistaDTO toggleActive(final Long id) {
		final Dentista dentista = this.exigir(id);
		dentista.setAtivo(dentista.getAtivo() == null || !dentista.getAtivo());
		return this.toDto(this.dentistaRepository.save(dentista));
	}

	public void excluir(final Long id) {
		this.exigir(id);
		if (this.consultaRepository.existsByDentistaId(id)) {
			throw new ConflitoException(
					"Não é possível excluir este dentista pois há consultas vinculadas a ele. Reatribua ou remova essas consultas antes de tentar novamente.");
		}
		this.dentistaRepository.deleteById(id);
	}

	private Dentista exigir(final Long id) {
		final Dentista dentista = this.dentistaRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Dentista com id: " + id + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(dentista.getClinicaId())) {
			throw new NotFoundException("Dentista com id: " + id + " não encontrado!");
		}
		return dentista;
	}

	private void validar(final DentistaDTO dto, final Long idAtual) {
		if (dto.getNome() == null || dto.getNome().isBlank()) {
			throw new IllegalArgumentException("nome é obrigatório");
		}
		if (dto.getCro() == null || dto.getCro().isBlank()) {
			throw new IllegalArgumentException("cro é obrigatório");
		}
		if (dto.getCorAgenda() == null || !HEX_COLOR.matcher(dto.getCorAgenda().trim()).matches()) {
			throw new IllegalArgumentException("corAgenda deve ser um hexadecimal válido, ex: #4f6bed");
		}

		// CRO é único GLOBALMENTE no banco (constraint em Dentista.cro), não só dentro
		// da clínica atual — a checagem precisa bater com o escopo real da constraint,
		// senão um CRO já usado em outra clínica passa aqui e quebra no INSERT.
		final boolean croEmUso = idAtual == null ? this.dentistaRepository.existsByCro(dto.getCro().trim())
				: this.dentistaRepository.existsByCroAndIdNot(dto.getCro().trim(), idAtual);
		if (croEmUso) {
			throw new ConflitoException("Já existe um dentista cadastrado com este CRO");
		}

		if (dto.getPercentualComissao() != null && (dto.getPercentualComissao() < 0 || dto.getPercentualComissao() > 100)) {
			throw new IllegalArgumentException("percentualComissao deve estar entre 0 e 100");
		}

		if (dto.getHorarioAtendimento() != null) {
			for (final HorarioAtendimentoDTO horario : dto.getHorarioAtendimento()) {
				if (horario.getDiaSemana() == null || horario.getDiaSemana() < 0 || horario.getDiaSemana() > 6) {
					throw new IllegalArgumentException("horarioAtendimento.diaSemana deve ser um número de 0 (domingo) a 6 (sábado)");
				}
				if (horario.getHoraInicio() == null || horario.getHoraFim() == null) {
					throw new IllegalArgumentException("horarioAtendimento.horaInicio e horaFim são obrigatórios");
				}
				if (!horario.getHoraInicio().isBefore(horario.getHoraFim())) {
					throw new IllegalArgumentException("horarioAtendimento.horaInicio deve ser antes de horaFim");
				}
			}
		}
	}

	private List<HorarioAtendimento> paraEntidade(final List<HorarioAtendimentoDTO> horarios) {
		if (horarios == null) {
			return new ArrayList<>();
		}
		return horarios.stream()
				.map(h -> HorarioAtendimento.builder().diaSemana(h.getDiaSemana()).horaInicio(h.getHoraInicio())
						.horaFim(h.getHoraFim()).build())
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private List<HorarioAtendimentoDTO> paraDto(final List<HorarioAtendimento> horarios) {
		if (horarios == null || horarios.isEmpty()) {
			return Collections.emptyList();
		}
		return horarios.stream()
				.map(h -> HorarioAtendimentoDTO.builder().diaSemana(h.getDiaSemana()).horaInicio(h.getHoraInicio())
						.horaFim(h.getHoraFim()).build())
				.collect(Collectors.toList());
	}

	private DentistaDTO toDto(final Dentista dentista) {
		return DentistaDTO.builder().id(dentista.getId()).nome(dentista.getNome()).cro(dentista.getCro())
				.especialidade(dentista.getEspecialidade()).telefone(dentista.getTelefone())
				.email(dentista.getEmail()).corAgenda(dentista.getCorAgenda()).ativo(dentista.getAtivo())
				.observacoes(dentista.getObservacoes()).criadoEm(dentista.getCriadoEm())
				.horarioAtendimento(this.paraDto(dentista.getHorarioAtendimento()))
				.percentualComissao(dentista.getPercentualComissao()).fotoUrl(dentista.getFotoUrl()).build();
	}
}
