package com.msp.odonto_pro.dto.usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;

import com.msp.odonto_pro.entity.usuario.Usuario;
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
    private Long clinicaId;
    private String clinicaNome;

    @JsonProperty("ativo")
    private boolean ativo;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private List<String> authorities;

    public UserDTO(final Usuario usuario) {
        this(usuario, null);
    }

    public UserDTO(final Usuario usuario, final String clinicaNome) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.username = usuario.getUsername();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.avatar = usuario.getAvatar();
        this.role = usuario.getRole();
        this.clinicaId = usuario.getClinicaId();
        this.clinicaNome = clinicaNome;
        this.ativo = usuario.isAtivo();
        this.criadoEm = usuario.getCriadoEm();
        this.atualizadoEm = usuario.getAtualizadoEm();
        this.authorities = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    @JsonProperty("ativo")
    public boolean isAtivo() {
        return this.ativo;
    }
}
