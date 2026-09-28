package com.msp.odonto_pro.dto.usuario;

import com.msp.odonto_pro.entity.usuario.Usuario;

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
        this.user = new UserDTO(usuario, null);
    }

    public LoginResponse(final String token, final Usuario usuario, final String clinicaNome) {
        this.token = token;
        this.user = new UserDTO(usuario, clinicaNome);
    }
}