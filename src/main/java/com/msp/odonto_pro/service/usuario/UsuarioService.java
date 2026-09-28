package com.msp.odonto_pro.service.usuario;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.msp.odonto_pro.dto.usuario.UserDTO;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.enums.Role;
import com.msp.odonto_pro.exceptions.ConflitoException;
import com.msp.odonto_pro.exceptions.ForbiddenOperationException;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.usuario.UsuarioRepository;
import com.msp.odonto_pro.security.SecurityUtils;
import com.msp.odonto_pro.util.ArquivoUploadUtil;
import com.msp.odonto_pro.util.PdfReportUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

	private static final String DIRETORIO_AVATARES = "avatares";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UserDTO atualizarPerfilProprio(final Usuario usuario, final String nome, final String email,
			final String telefone, final MultipartFile avatarFile) throws IOException {
		usuario.setNome(nome);
		usuario.setEmail(email);
		usuario.setTelefone(telefone);

		if (avatarFile != null && !avatarFile.isEmpty()) {
			usuario.setAvatar(ArquivoUploadUtil.salvar(avatarFile, DIRETORIO_AVATARES));
		}

		this.usuarioRepository.save(usuario);
		return new UserDTO(usuario);
	}

	public List<UserDTO> listar() {
		final Long clinicaId = SecurityUtils.clinicaIdAtual();
		return this.usuarioRepository.findAll()
				.stream()
				.filter(u -> clinicaId.equals(u.getClinicaId()))
				.map(UserDTO::new)
				.collect(Collectors.toList());
	}

	public UserDTO criar(final String nome, final String username, final String email, final String telefone,
			final String password, final Role role, final boolean ativo, final MultipartFile avatarFile)
			throws IOException {

		if (role == Role.SUPER_ADMIN) {
			throw new ForbiddenOperationException("Não é possível criar um SUPER_ADMIN por aqui");
		}
		if (this.usuarioRepository.existsByUsername(username)) {
			throw new ConflitoException("Username já existe");
		}
		if (this.usuarioRepository.existsByEmail(email)) {
			throw new ConflitoException("Email já cadastrado");
		}

		final Usuario usuario = new Usuario();
		usuario.setNome(nome);
		usuario.setUsername(username);
		usuario.setEmail(email);
		usuario.setTelefone(telefone);
		usuario.setPassword(this.passwordEncoder.encode(password));
		usuario.setRole(role);
		usuario.setAtivo(ativo);
		usuario.setClinicaId(SecurityUtils.clinicaIdAtual());

		if (avatarFile != null && !avatarFile.isEmpty()) {
			usuario.setAvatar(ArquivoUploadUtil.salvar(avatarFile, DIRETORIO_AVATARES));
		}

		final Usuario saved = this.usuarioRepository.save(usuario);
		return new UserDTO(saved);
	}

	public UserDTO atualizar(final Long id, final String nome, final String email, final String telefone,
			final Role role, final boolean ativo, final String password, final MultipartFile avatarFile)
			throws IOException {

		if (role == Role.SUPER_ADMIN) {
			throw new ForbiddenOperationException("Não é possível promover um usuário a SUPER_ADMIN por aqui");
		}

		final Usuario usuario = this.buscarDaMinhaClinica(id);

		usuario.setNome(nome);
		usuario.setEmail(email);
		usuario.setTelefone(telefone);
		usuario.setRole(role);
		usuario.setAtivo(ativo);

		if (password != null && !password.isEmpty()) {
			usuario.setPassword(this.passwordEncoder.encode(password));
		}

		if (avatarFile != null && !avatarFile.isEmpty()) {
			usuario.setAvatar(ArquivoUploadUtil.salvar(avatarFile, DIRETORIO_AVATARES));
		}

		final Usuario saved = this.usuarioRepository.save(usuario);
		return new UserDTO(saved);
	}

	public UserDTO toggleActive(final Long id, final Usuario usuarioAutenticado) {
		if (usuarioAutenticado.getId().equals(id)) {
			throw new ForbiddenOperationException("Você não pode desativar sua própria conta");
		}

		final Usuario usuario = this.buscarDaMinhaClinica(id);
		usuario.setAtivo(!usuario.isAtivo());
		final Usuario saved = this.usuarioRepository.save(usuario);
		return new UserDTO(saved);
	}

	public void deletar(final Long id, final Usuario usuarioAutenticado) {
		if (usuarioAutenticado.getId().equals(id)) {
			throw new ForbiddenOperationException("Você não pode excluir sua própria conta");
		}

		this.buscarDaMinhaClinica(id);
		this.usuarioRepository.deleteById(id);
	}

	public UserDTO alterarRole(final Long id, final Role novaRole) {
		if (novaRole == Role.SUPER_ADMIN) {
			throw new ForbiddenOperationException("Não é possível promover um usuário a SUPER_ADMIN por aqui");
		}

		final Usuario usuario = this.buscarDaMinhaClinica(id);
		usuario.setRole(novaRole);
		final Usuario saved = this.usuarioRepository.save(usuario);
		return new UserDTO(saved);
	}

	/** Busca um usuário garantindo que pertence à clínica do usuário autenticado. */
	private Usuario buscarDaMinhaClinica(final Long id) {
		final Long clinicaId = SecurityUtils.clinicaIdAtual();
		final Usuario usuario = this.usuarioRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
		if (!clinicaId.equals(usuario.getClinicaId())) {
			throw new NotFoundException("Usuário não encontrado");
		}
		return usuario;
	}

	public byte[] gerarRelatorioPdf() {
		final Long clinicaId = SecurityUtils.clinicaIdAtual();
		final List<Usuario> usuarios = this.usuarioRepository.findAll().stream()
				.filter(u -> clinicaId.equals(u.getClinicaId()))
				.collect(Collectors.toList());

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			final Document document = PdfReportUtil.novoDocumento(out);
			document.add(PdfReportUtil.titulo("Relatório de Usuários"));
			document.add(new Paragraph(" "));

			final PdfPTable table = PdfReportUtil.novaTabela(new float[] { 3f, 2f, 3f, 2f, 1.5f }, "Nome",
					"Username", "Email", "Role", "Ativo");

			for (final Usuario usuario : usuarios) {
				PdfReportUtil.celula(table, usuario.getNome());
				PdfReportUtil.celula(table, usuario.getUsername());
				PdfReportUtil.celula(table, usuario.getEmail());
				PdfReportUtil.celula(table, usuario.getRole() != null ? usuario.getRole().name() : "");
				PdfReportUtil.celula(table, usuario.isAtivo() ? "Sim" : "Não");
			}

			document.add(table);
			document.close();
			return out.toByteArray();
		} catch (final DocumentException | IOException e) {
			throw new RuntimeException("Erro ao gerar relatório de usuários", e);
		}
	}
}
