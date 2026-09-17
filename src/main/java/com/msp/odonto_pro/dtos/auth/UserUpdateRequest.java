package com.msp.odonto_pro.dtos.auth;

import com.msp.odonto_pro.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String nome;
    private String email;
    private String telefone;
    private Role role;
    private boolean ativo;
    private String password;
}