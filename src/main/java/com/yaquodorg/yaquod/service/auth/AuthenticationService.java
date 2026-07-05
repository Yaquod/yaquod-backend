package com.yaquodorg.yaquod.service.auth;

import com.yaquodorg.yaquod.dtos.auth.GoogleIdTokenDto;
import com.yaquodorg.yaquod.dtos.auth.LoginUserDto;
import com.yaquodorg.yaquod.dtos.auth.RegisterUserDto;
import com.yaquodorg.yaquod.dtos.auth.ResetPasswordDto;
import com.yaquodorg.yaquod.dtos.auth.VehicleLoginDto;
import com.yaquodorg.yaquod.dtos.auth.VerifyCodeDto;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.LoginResponse;
import com.yaquodorg.yaquod.response.VehicleLoginResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AuthenticationService {
    LoginResponse login(LoginUserDto loginUserDto);

    LoginResponse googleLogin(GoogleIdTokenDto request)
            throws GeneralSecurityException, IOException;

    VehicleLoginResponse vehicleLogin(VehicleLoginDto vehicleLoginDto);

    User signup(RegisterUserDto registerUserDto, String role);

    LoginResponse refreshToken(String authHeader);

    boolean verifyUser(VerifyCodeDto verifyCodeDto);

    void regenerateOtp(String email);

    boolean resetPassword(ResetPasswordDto resetPasswordDto);

    User getMe(String authHeader);
}
