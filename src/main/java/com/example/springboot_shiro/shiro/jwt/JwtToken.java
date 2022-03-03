package com.example.springboot_shiro.shiro.jwt;

import org.apache.shiro.authc.AuthenticationToken;
//
public class JwtToken implements AuthenticationToken {
    /**
     * Token
     */
    private String token;

    public JwtToken(String token) {
        this.token = token;
    }
    @Override
    public Object getPrincipal() {

        return null;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
