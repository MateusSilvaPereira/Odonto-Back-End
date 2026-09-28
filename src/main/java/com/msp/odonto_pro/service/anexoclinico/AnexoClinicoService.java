package com.msp.odonto_pro.service.anexoclinico;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.msp.odonto_pro.dto.anexoclinico.AnexoClinicoDTO;
import com.msp.odonto_pro.entity.anexoclinico.AnexoClinico;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.anexoclinico.AnexoClinicoRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnexoClinicoService {

	private static final Logger log = LoggerFactory.getLogger(AnexoClinicoService.class);

	private static final long TAMANHO_MAXIMO_BYTES = 20L * 1024 * 1024; // 20MB (radiografias/fotos em alta resolução)
	private static final Set<String> EXTENSOES_IMAGEM = Set.of("jpg", "jpeg", "png");
	private static final Set<String> EXTENSOES_DOCUMENTO = Set.of("pdf", "doc", "docx");
	private static final String DIRETORIO_UPLOAD = "uploads/anexos/";

	private final AnexoClinicoRepository anexoClinicoRepository;
	private final PacienteRepository pacienteRepository;

	public List<AnexoClinicoDTO> listar(final Long pacienteId) {
		this.exigirPaciente(pacienteId);
		return this.anexoClinicoRepository.findByPacienteIdOrderByCriadoEmDesc(pacienteId).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	public AnexoClinicoDTO upload(final Long pacienteId, final MultipartFile arquivo, final Usuario usuarioLogado)
			throws IOException {
		final Paciente paciente = this.exigirPaciente(pacienteId);

		if (arquivo == null || arquivo.isEmpty()) {
			throw new IllegalArgumentException("arquivo é obrigatório");
		}
		if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
			throw new IllegalArgumentException("Arquivo excede o tamanho máximo permitido (20MB)");
		}

		final String nomeOriginal = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename()
				: "arquivo";
		final String extensao = this.extrairExtensao(nomeOriginal);
		final String tipo = this.classificarTipo(extensao);

		final String url = this.salvarArquivo(arquivo, extensao);

		final AnexoClinico anexo = AnexoClinico.builder().paciente(paciente).nome(nomeOriginal).tipo(tipo).url(url)
				.tamanho(arquivo.getSize()).criadoPor(usuarioLogado != null ? usuarioLogado.getNome() : null).build();

		return this.toDto(this.anexoClinicoRepository.save(anexo));
	}

	public void excluir(final Long pacienteId, final Long anexoId) {
		this.exigirPaciente(pacienteId);

		final AnexoClinico anexo = this.anexoClinicoRepository.findById(anexoId)
				.filter(a -> a.getPaciente().getId().equals(pacienteId))
				.orElseThrow(() -> new NotFoundException("Anexo com id: " + anexoId + " não encontrado!"));

		this.anexoClinicoRepository.delete(anexo);
		this.removerArquivoFisico(anexo.getUrl());
	}

	private Paciente exigirPaciente(final Long pacienteId) {
		final Paciente paciente = this.pacienteRepository.findById(pacienteId)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(paciente.getClinicaId())) {
			throw new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!");
		}
		return paciente;
	}

	private String extrairExtensao(final String nomeArquivo) {
		final int ponto = nomeArquivo.lastIndexOf('.');
		return ponto >= 0 && ponto < nomeArquivo.length() - 1 ? nomeArquivo.substring(ponto + 1).toLowerCase(Locale.ROOT)
				: "";
	}

	private String classificarTipo(final String extensao) {
		if (EXTENSOES_IMAGEM.contains(extensao)) {
			return "imagem";
		}
		if (EXTENSOES_DOCUMENTO.contains(extensao)) {
			return "documento";
		}
		throw new IllegalArgumentException(
				"Tipo de arquivo não permitido. Use: jpg, jpeg, png, pdf, doc ou docx");
	}

	private String salvarArquivo(final MultipartFile arquivo, final String extensao) throws IOException {
		Files.createDirectories(Paths.get(DIRETORIO_UPLOAD));

		final String nomeArmazenado = UUID.randomUUID() + (extensao.isEmpty() ? "" : "." + extensao);
		final Path caminho = Paths.get(DIRETORIO_UPLOAD, nomeArmazenado);
		Files.write(caminho, arquivo.getBytes());

		return "/" + DIRETORIO_UPLOAD + nomeArmazenado;
	}

	private void removerArquivoFisico(final String url) {
		try {
			final String caminhoRelativo = url.startsWith("/") ? url.substring(1) : url;
			Files.deleteIfExists(Paths.get(caminhoRelativo));
		} catch (final IOException e) {
			log.warn("Falha ao remover arquivo físico do anexo ({}): {}", url, e.getMessage());
		}
	}

	private AnexoClinicoDTO toDto(final AnexoClinico anexo) {
		return AnexoClinicoDTO.builder().id(anexo.getId()).pacienteId(anexo.getPaciente().getId())
				.nome(anexo.getNome()).tipo(anexo.getTipo()).url(anexo.getUrl()).tamanho(anexo.getTamanho())
				.criadoEm(anexo.getCriadoEm()).criadoPor(anexo.getCriadoPor()).build();
	}
}
