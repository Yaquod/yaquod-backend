package com.yaquodorg.yaquod.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoogleLoginDto {
    private String email;
    private String name;
    private String givenName;
    private String familyName;
}
