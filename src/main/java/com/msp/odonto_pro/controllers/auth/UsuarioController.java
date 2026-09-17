package com.msp.odonto_pro.controllers.auth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.msp.odonto_pro.dtos.auth.LoginResponse.UserDTO;
import com.msp.odonto_pro.entity.Usuario;
import com.msp.odonto_pro.enums.Role;
import com.msp.odonto_pro.repositorys.UsuarioRepository;
import com.msp.odonto_pro.util.PdfReportUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/atualizar")
    public ResponseEntity<UserDTO> atualizar(
            @RequestParam("nome") final String nome,
            @RequestParam("email") final String email,
            @RequestParam("telefone") final String telefone,
            @RequestParam(value = "avatar", required = false) final MultipartFile avatarFile,
            final Authentication authentication) throws IOException {

        final Usuario usuario = (Usuario) authentication.getPrincipal();

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            usuario.setAvatar(salvarArquivo(avatarFile));
        }

        this.usuarioRepository.save(usuario);
        return ResponseEntity.ok(new UserDTO(usuario));
    }

    @PreAuthorize("hasAuthority('read:usuario')")
    @GetMapping("/listar")
    public ResponseEntity<List<UserDTO>> listar() {
        final List<UserDTO> usuarios = this.usuarioRepository.findAll()
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @PreAuthorize("hasAuthority('write:usuario')")
    @PostMapping("/criar")
    public ResponseEntity<UserDTO> criar(
            @RequestParam("nome") final String nome,
            @RequestParam("username") final String username,
            @RequestParam("email") final String email,
            @RequestParam(value = "telefone", required = false) final String telefone,
            @RequestParam("password") final String password,
            @RequestParam("role") final Role role,
            @RequestParam(value = "ativo", defaultValue = "true") final boolean ativo,
            @RequestParam(value = "avatar", required = false) final MultipartFile avatarFile) throws IOException {

        if (this.usuarioRepository.existsByUsername(username)) {
            throw new RuntimeException("Username já existe");
        }
        if (this.usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("Email já cadastrado");
        }

        final Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setPassword(this.passwordEncoder.encode(password));
        usuario.setRole(role);
        usuario.setAtivo(ativo);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            usuario.setAvatar(salvarArquivo(avatarFile));
        }

        final Usuario saved = this.usuarioRepository.save(usuario);
        return ResponseEntity.ok(new UserDTO(saved));
    }

    @PreAuthorize("hasAuthority('write:usuario')")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> atualizarUsuario(
            @PathVariable final Long id,
            @RequestParam("nome") final String nome,
            @RequestParam("email") final String email,
            @RequestParam(value = "telefone", required = false) final String telefone,
            @RequestParam("role") final Role role,
            @RequestParam("ativo") final boolean ativo,
            @RequestParam(value = "password", required = false) final String password,
            @RequestParam(value = "avatar", required = false) final MultipartFile avatarFile) throws IOException {

        System.out.println("=== ATUALIZAR USUÁRIO ===");
        System.out.println("ID: " + id);
        System.out.println("Ativo recebido: " + ativo);

        final Usuario usuario = this.usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setRole(role);
        usuario.setAtivo(ativo);

        System.out.println("Ativo setado no usuario: " + usuario.isAtivo());

        if (password != null && !password.isEmpty()) {
            usuario.setPassword(this.passwordEncoder.encode(password));
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            usuario.setAvatar(salvarArquivo(avatarFile));
        }

        final Usuario saved = this.usuarioRepository.save(usuario);
        System.out.println("Ativo salvo no banco: " + saved.isAtivo());

        final UserDTO dto = new UserDTO(saved);
        System.out.println("Ativo no DTO: " + dto.isAtivo());

        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAuthority('write:usuario')")
    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<UserDTO> toggleActive(
            @PathVariable final Long id,
            final Authentication authentication) {

        final Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();

        if (usuarioAutenticado.getId().equals(id)) {
            throw new RuntimeException("Você não pode desativar sua própria conta");
        }

        final Usuario usuario = this.usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setAtivo(!usuario.isAtivo());
        final Usuario saved = this.usuarioRepository.save(usuario);
        return ResponseEntity.ok(new UserDTO(saved));
    }

    @PreAuthorize("hasAuthority('delete:usuario')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable final Long id,
            final Authentication authentication) {
        
        final Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();

        if (usuarioAutenticado.getId().equals(id)) {
            throw new RuntimeException("Você não pode excluir sua própria conta");
        }

        if (!this.usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        
        this.usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('write:usuario')")
    @PutMapping("/alterar-role/{id}")
    public ResponseEntity<UserDTO> alterarRole(
            @PathVariable final Long id,
            @RequestBody final RoleRequest request) {

        final Usuario usuario = this.usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setRole(request.getRole());
        final Usuario saved = this.usuarioRepository.save(usuario);
        return ResponseEntity.ok(new UserDTO(saved));
    }

    @PreAuthorize("hasAuthority('read:usuario')")
    @GetMapping("/relatorio")
    public ResponseEntity<byte[]> relatorio() {
        final byte[] pdf = this.gerarRelatorioPdf();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-usuarios.pdf").body(pdf);
    }

    private byte[] gerarRelatorioPdf() {
        final List<Usuario> usuarios = this.usuarioRepository.findAll();

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

    private String salvarArquivo(MultipartFile file) throws IOException {
        final String uploadDir = "uploads/";
        Files.createDirectories(Paths.get(uploadDir));

        final String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        final Path filePath = Paths.get(uploadDir, fileName);
        Files.write(filePath, file.getBytes());

        return "/uploads/" + fileName;
    }

    @lombok.Data
    public static class RoleRequest {
        private Role role;
    }
}