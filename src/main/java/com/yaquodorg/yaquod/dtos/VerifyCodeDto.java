package com.yaquodorg.yaquod.dtos;

import lombok.Data;

@Data
public class VerifyCodeDto {
    private String email;
    private int code;
}
