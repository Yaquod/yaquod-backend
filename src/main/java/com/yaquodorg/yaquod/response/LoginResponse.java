package com.yaquodorg.yaquod.response;

import com.yaquodorg.yaquod.entity.User;
import java.util.Date;
import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Date accessTokenExpiresIn;
    private Date refreshTokenExpiresIn;
    private User user;
}
