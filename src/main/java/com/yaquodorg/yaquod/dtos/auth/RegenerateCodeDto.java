package com.yaquodorg.yaquod.dtos.auth;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class RegenerateCodeDto {
    @Email(message = "Email must be valid")
    private String email;
}
