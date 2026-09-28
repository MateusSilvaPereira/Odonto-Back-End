package com.msp.odonto_pro.service.convenio;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.convenio.ConvenioDTO;
import com.msp.odonto_pro.entity.convenio.Convenio;
import com.msp.odonto_pro.exceptions.ConflitoException;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.convenio.ConvenioRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConvenioService {

	private final ConvenioRepository convenioRepository;
	private final PacienteRepository pacienteRepository;

	public List<ConvenioDTO> listar() {
		return this.convenioRepository.findByClinicaId(SecurityUtils.clinicaIdAtual()).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	public ConvenioDTO criar(final ConvenioDTO dto) {
		this.validar(dto);

		final Convenio convenio = Convenio.builder().clinicaId(SecurityUtils.clinicaIdAtual())
				.nome(dto.getNome().trim()).registroANS(dto.getRegistroANS())
				.telefone(dto.getTelefone()).observacoes(dto.getObservacoes())
				.ativo(dto.getAtivo() == null || dto.getAtivo()).build();

		return this.toDto(this.convenioRepository.save(convenio));
	}

	public ConvenioDTO atualizar(final Long id, final ConvenioDTO dto) {
		this.validar(dto);

		final Convenio convenio = this.exigir(id);
		convenio.setNome(dto.getNome().trim());
		convenio.setRegistroANS(dto.getRegistroANS());
		convenio.setTelefone(dto.getTelefone());
		convenio.setObservacoes(dto.getObservacoes());
		if (dto.getAtivo() != null) {
			convenio.setAtivo(dto.getAtivo());
		}

		return this.toDto(this.convenioRepository.save(convenio));
	}

	public ConvenioDTO toggleActive(final Long id) {
		final Convenio convenio = this.exigir(id);
		convenio.setAtivo(convenio.getAtivo() == null || !convenio.getAtivo());
		return this.toDto(this.convenioRepository.save(convenio));
	}

	public void excluir(final Long id) {
		this.exigir(id);
		if (this.pacienteRepository.existsByConvenioId(id)) {
			throw new ConflitoException(
					"Não é possível excluir este convênio pois há pacientes vinculados a ele. Reatribua ou remova esse vínculo antes de tentar novamente.");
		}
		this.convenioRepository.deleteById(id);
	}

	private Convenio exigir(final Long id) {
		final Convenio convenio = this.convenioRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Convênio com id: " + id + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(convenio.getClinicaId())) {
			throw new NotFoundException("Convênio com id: " + id + " não encontrado!");
		}
		return convenio;
	}

	private void validar(final ConvenioDTO dto) {
		if (dto.getNome() == null || dto.getNome().isBlank()) {
			throw new IllegalArgumentException("nome é obrigatório");
		}
	}

	private ConvenioDTO toDto(final Convenio convenio) {
		return ConvenioDTO.builder().id(convenio.getId()).nome(convenio.getNome())
				.registroANS(convenio.getRegistroANS()).telefone(convenio.getTelefone())
				.observacoes(convenio.getObservacoes()).ativo(convenio.getAtivo()).criadoEm(convenio.getCriadoEm())
				.build();
	}
}
