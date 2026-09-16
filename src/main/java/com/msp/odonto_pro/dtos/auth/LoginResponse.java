package com.msp.odonto_pro.dtos.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;

import com.msp.odonto_pro.entity.Usuario;
import com.msp.odonto_pro.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private UserDTO user;

    public LoginResponse(final String token, final Usuario usuario) {
        this.token = token;
        this.user = new UserDTO(usuario);
    }

    @Data
    @NoArgsConstructor
    public static class UserDTO {
        private Long id;
        private String nome;
        private String username;
        private String email;
        private String telefone;
        private String avatar;
        private Role role;
        
        @JsonProperty("ativo")
        private boolean ativo;
        
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
        private List<String> authorities;

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

        @JsonProperty("ativo")
        public boolean isAtivo() {
            return this.ativo;
        }
    }
}