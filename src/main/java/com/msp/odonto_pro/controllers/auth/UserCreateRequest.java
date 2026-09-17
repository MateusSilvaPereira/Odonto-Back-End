package com.msp.odonto_pro.controllers.auth;



import com.msp.odonto_pro.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    private String nome;
    private String username;
    private String email;
    private String telefone;
    private String password;
    private Role role;
    private boolean ativo = true;
}