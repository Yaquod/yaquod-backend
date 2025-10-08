package com.yaquodorg.yaquod.dtos;

import lombok.Data;

@Data
public class LoginUserDto {
    private String email;
    private String password;
    private String fcmToken;
}
