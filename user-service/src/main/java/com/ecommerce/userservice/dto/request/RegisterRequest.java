package com.ecommerce.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class RegisterRequest implements Serializable {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
