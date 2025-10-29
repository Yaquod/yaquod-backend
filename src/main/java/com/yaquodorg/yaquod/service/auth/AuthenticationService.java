package com.yaquodorg.yaquod.service.auth;

import com.yaquodorg.yaquod.dtos.*;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(LoginUserDto loginUserDto);

    LoginResponse googleLogin(GoogleLoginDto googleLoginDto);

    User signup(RegisterUserDto registerUserDto, String role);

    LoginResponse refreshToken(String authHeader);

    boolean verifyUser(VerifyCodeDto verifyCodeDto);

    void regenerateOtp(String email);

    boolean resetPassword(ResetPasswordDto resetPasswordDto);
}
