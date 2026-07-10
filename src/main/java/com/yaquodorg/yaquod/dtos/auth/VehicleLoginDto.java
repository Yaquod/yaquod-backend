package com.yaquodorg.yaquod.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleLoginDto {
    @NotBlank private String apiKey;
    @NotBlank private String apiSecret;
}
