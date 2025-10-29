package com.yaquodorg.yaquod.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginDto {
    private String email;
    private String name;
    private String givenName;
    private String familyName;
}
