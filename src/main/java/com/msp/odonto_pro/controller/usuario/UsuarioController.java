package com.msp.odonto_pro.controller.usuario;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.msp.odonto_pro.dto.usuario.RoleRequest;
import com.msp.odonto_pro.dto.usuario.UserDTO;
import com.msp.odonto_pro.dto.usuario.UserCreateRequest;
import com.msp.odonto_pro.dto.usuario.UserUpdateRequest;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.enums.Role;
import com.msp.odonto_pro.service.usuario.PasswordResetService;
import com.msp.odonto_pro.service.usuario.UsuarioService;
import com.msp.odonto_pro.web.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.V1 + "/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PasswordResetService passwordResetService;

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ResponseEntity<UserDTO> atualizar(
            @RequestParam("nome") final String nome,
            @RequestParam("email") final String email,
            @RequestParam("telefone") final String telefone,
            @RequestParam(value = "avatar", required = false) final MultipartFile avatarFile,
            final Authentication authentication) throws IOException {

        final Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(this.usuarioService.atualizarPerfilProprio(usuario, nome, email, telefone, avatarFile));
    }

    @PreAuthorize("hasAuthority('read:usuario')")
    @GetMapping
    public ResponseEntity<List<UserDTO>> listar() {
        return ResponseEntity.ok(this.usuarioService.listar());
    }

    /**
     * Criação via JSON puro (sem foto). O front manda "Content-Type:
     * application/json" quando não há avatar novo para enviar.
     */
    @PreAuthorize("hasAuthority('write:usuario')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> criarJson(@Valid @RequestBody final UserCreateRequest request) throws IOException {
        final UserDTO criado = this.usuarioService.criar(request.getNome(), request.getUsername(),
                request.getEmail(), request.getTelefone(), request.getPassword(), request.getRole(),
                request.isAtivo(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Criação via multipart/form-data, usada quando o front envia um avatar
     * novo junto com os demais campos.
     */
    @PreAuthorize("hasAuthority('write:usuario')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> criar(
            @RequestParam("nome") final String nome,
            @RequestParam("username") final String username,
            @RequestParam("email") final String email,
            @RequestParam(value = "telefone", required = false) final String telefone,
            @RequestParam("password") final String password,
            @RequestParam("role") final Role role,
            @RequestParam(value = "ativo", defaultValue = "true") final boolean ativo,
            @RequestParam(value = "avatar", required = false) final MultipartFile avatarFile) throws IOException {

        final UserDTO criado = this.usuarioService.criar(nome, username, email, telefone, password, role, ativo,
                avatarFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Edição via JSON puro (sem foto nova). Antes deste fix, o endpoint só
     * aceitava @RequestParam (form-data), então uma edição comum via JSON
     * (o caso mais frequente no front) estourava
     * MissingServletRequestParameterException e voltava como 500 genérico.
     */
    @PreAuthorize("hasAuthority('write:usuario')")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> atualizarUsuarioJson(@PathVariable final Long id,
            @Valid @RequestBody final UserUpdateRequest request) throws IOException {
        return ResponseEntity.ok(this.usuarioService.atualizar(id, request.getNome(), request.getEmail(),
                request.getTelefone(), request.getRole(), request.isAtivo(), request.getPassword(), null));
    }

    /**
     * Edição via multipart/form-data, usada quando o front envia um avatar
     * novo junto com os demais campos.
     */
    @PreAuthorize("hasAuthority('write:usuario')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> atualizarUsuario(
            @PathVariable final Long id,
            @RequestParam("nome") final String nome,
            @RequestParam("email") final String email,
            @RequestParam(value = "telefone", required = false) final String telefone,
            @RequestParam("role") final Role role,
            @RequestParam("ativo") final boolean ativo,
            @RequestParam(value = "password", required = false) final String password,
            @RequestParam(value = "avatar", required = false) final MultipartFile avatarFile) throws IOException {

        return ResponseEntity.ok(this.usuarioService.atualizar(id, nome, email, telefone, role, ativo, password,
                avatarFile));
    }

    @PreAuthorize("hasAuthority('write:usuario')")
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<UserDTO> toggleActive(
            @PathVariable final Long id,
            final Authentication authentication) {

        final Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(this.usuarioService.toggleActive(id, usuarioAutenticado));
    }

    @PreAuthorize("hasAuthority('delete:usuario')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable final Long id,
            final Authentication authentication) {

        final Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        this.usuarioService.deletar(id, usuarioAutenticado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('write:usuario')")
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDTO> alterarRole(
            @PathVariable final Long id,
            @RequestBody final RoleRequest request) {

        return ResponseEntity.ok(this.usuarioService.alterarRole(id, request.getRole()));
    }

    /**
     * Dispara pra outro usuário o mesmo fluxo de e-mail de "esqueci minha
     * senha" — substitui a antiga edição direta de senha de terceiros.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Enviar redefinição de senha para outro usuário.")
    @PostMapping("/{id}/enviar-redefinicao-senha")
    public ResponseEntity<Void> enviarRedefinicaoSenha(@PathVariable final Long id,
            final Authentication authentication) {
        final Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        this.passwordResetService.enviarRedefinicaoParaUsuario(id, usuarioAutenticado);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('read:usuario')")
    @GetMapping("/relatorio")
    public ResponseEntity<byte[]> relatorio() {
        final byte[] pdf = this.usuarioService.gerarRelatorioPdf();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-usuarios.pdf").body(pdf);
    }
}
