package com.yaquodorg.yaquod.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * DTO for user registration requests.
 * Ensures all fields meet basic validation constraints.
 */
@Data
public class RegisterUserDto {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String password;

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 30, message = "First name must be less than 30 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 30, message = "Last name must be less than 30 characters")
    private String lastName;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone number must be between 10 and 15 digits and may start with +"
    )
    private String phoneNumber;
}
