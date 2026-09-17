package com.msp.odonto_pro.dtos;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

import com.msp.odonto_pro.entity.Usuario;
import com.msp.odonto_pro.enums.Role;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String nome;
    private String username;
    private String email;
    private String telefone;
    private String avatar;
    private Role role;
    private boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private java.util.List<String> authorities;

    public UserDTO(final Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.username = usuario.getUsername();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.avatar = usuario.getAvatar();
        this.role = usuario.getRole();
        this.ativo = usuario.isAtivo();
        this.criadoEm = usuario.getCriadoEm();
        this.atualizadoEm = usuario.getAtualizadoEm();
        this.authorities = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}