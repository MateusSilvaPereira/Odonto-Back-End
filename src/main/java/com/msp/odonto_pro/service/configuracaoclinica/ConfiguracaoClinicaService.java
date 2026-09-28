package com.msp.odonto_pro.service.configuracaoclinica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.msp.odonto_pro.dto.configuracaoclinica.ConfiguracaoClinicaDTO;
import com.msp.odonto_pro.entity.configuracaoclinica.ConfiguracaoClinica;
import com.msp.odonto_pro.repository.configuracaoclinica.ConfiguracaoClinicaRepository;

import lombok.RequiredArgsConstructor;

/** Configuração singleton da clínica (ver ConfiguracaoClinica). */
@Service
@RequiredArgsConstructor
public class ConfiguracaoClinicaService {

	private static final Long ID_UNICO = 1L;
	private static final String DIRETORIO_UPLOAD = "uploads/clinica/";

	private final ConfiguracaoClinicaRepository configuracaoClinicaRepository;

	public ConfiguracaoClinicaDTO obter() {
		return this.configuracaoClinicaRepository.findById(ID_UNICO).map(this::toDto)
				.orElseGet(() -> ConfiguracaoClinicaDTO.builder().build());
	}

	public ConfiguracaoClinicaDTO atualizar(final String nome, final String cnpj, final String telefone,
			final String endereco, final MultipartFile logoFile) throws IOException {
		if (nome == null || nome.isBlank()) {
			throw new IllegalArgumentException("nome é obrigatório");
		}

		final ConfiguracaoClinica config = this.configuracaoClinicaRepository.findById(ID_UNICO)
				.orElseGet(() -> ConfiguracaoClinica.builder().id(ID_UNICO).build());

		config.setNome(nome);
		config.setCnpj(cnpj);
		config.setTelefone(telefone);
		config.setEndereco(endereco);

		if (logoFile != null && !logoFile.isEmpty()) {
			config.setLogoUrl(this.salvarLogo(logoFile));
		}

		return this.toDto(this.configuracaoClinicaRepository.save(config));
	}

	private String salvarLogo(final MultipartFile logoFile) throws IOException {
		Files.createDirectories(Paths.get(DIRETORIO_UPLOAD));

		final String nomeArmazenado = UUID.randomUUID() + "_" + logoFile.getOriginalFilename();
		final Path caminho = Paths.get(DIRETORIO_UPLOAD, nomeArmazenado);
		Files.write(caminho, logoFile.getBytes());

		return "/" + DIRETORIO_UPLOAD + nomeArmazenado;
	}

	private ConfiguracaoClinicaDTO toDto(final ConfiguracaoClinica config) {
		return ConfiguracaoClinicaDTO.builder().nome(config.getNome()).cnpj(config.getCnpj())
				.telefone(config.getTelefone()).endereco(config.getEndereco()).logoUrl(config.getLogoUrl())
				.atualizadoEm(config.getAtualizadoEm()).build();
	}
}
