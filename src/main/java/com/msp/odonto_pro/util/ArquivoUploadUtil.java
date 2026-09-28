package com.msp.odonto_pro.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

/** Salva um arquivo enviado em uploads/&lt;diretorio&gt;/ e devolve o path relativo (ex: "/uploads/fotos/uuid_nome.jpg"). */
public class ArquivoUploadUtil {

	private ArquivoUploadUtil() {
	}

	public static String salvar(final MultipartFile arquivo, final String diretorio) throws IOException {
		final String pastaBase = "uploads/" + diretorio + "/";
		Files.createDirectories(Paths.get(pastaBase));

		final String nomeArmazenado = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
		final Path caminho = Paths.get(pastaBase, nomeArmazenado);
		Files.write(caminho, arquivo.getBytes());

		return "/" + pastaBase + nomeArmazenado;
	}

	/**
	 * Salva uma imagem enviada como data URL base64 (ex: "data:image/png;base64,iVBORw0...").
	 * O prefixo "data:...;base64," é opcional — se ausente, todo o valor é tratado como base64
	 * puro e {@code extensaoPadrao} é usado no nome do arquivo. Nulo/branco retorna null.
	 */
	public static String salvarBase64(final String dataUrl, final String diretorio, final String extensaoPadrao)
			throws IOException {
		if (dataUrl == null || dataUrl.isBlank()) {
			return null;
		}

		String base64 = dataUrl;
		String extensao = extensaoPadrao;

		final int virgula = dataUrl.indexOf(',');
		if (dataUrl.startsWith("data:") && virgula > -1) {
			final String meta = dataUrl.substring(5, virgula);
			base64 = dataUrl.substring(virgula + 1);

			final int barra = meta.indexOf('/');
			final int ponteVirgula = meta.indexOf(';');
			if (barra > -1 && ponteVirgula > barra) {
				extensao = meta.substring(barra + 1, ponteVirgula);
			}
		}

		final byte[] bytes = Base64.getDecoder().decode(base64);

		final String pastaBase = "uploads/" + diretorio + "/";
		Files.createDirectories(Paths.get(pastaBase));

		final String nomeArmazenado = UUID.randomUUID() + "." + extensao;
		final Path caminho = Paths.get(pastaBase, nomeArmazenado);
		Files.write(caminho, bytes);

		return "/" + pastaBase + nomeArmazenado;
	}
}
