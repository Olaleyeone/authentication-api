package com.olaleyeone.auth.data.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginApiRequest {

    @NotBlank
    private String identifier;
    @NotBlank
    private String password;
}
