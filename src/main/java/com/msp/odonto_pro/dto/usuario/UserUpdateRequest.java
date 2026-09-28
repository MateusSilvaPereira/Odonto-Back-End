package com.msp.odonto_pro.dto.usuario;

import com.msp.odonto_pro.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @NotBlank(message = "nome é obrigatório")
    private String nome;

    @NotBlank(message = "email é obrigatório")
    @Email(message = "email inválido")
    private String email;

    private String telefone;
    private Role role;
    private boolean ativo;

    /** Opcional: informado apenas quando o admin quer redefinir a senha do usuário. */
    private String password;
}