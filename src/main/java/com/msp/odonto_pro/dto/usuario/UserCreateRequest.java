package com.msp.odonto_pro.dto.usuario;

import com.msp.odonto_pro.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    @NotBlank(message = "nome é obrigatório")
    private String nome;

    @NotBlank(message = "username é obrigatório")
    private String username;

    @NotBlank(message = "email é obrigatório")
    @Email(message = "email inválido")
    private String email;

    private String telefone;

    @NotBlank(message = "password é obrigatório")
    @Size(min = 6, message = "password deve ter ao menos 6 caracteres")
    private String password;

    private Role role;
    private boolean ativo = true;
}