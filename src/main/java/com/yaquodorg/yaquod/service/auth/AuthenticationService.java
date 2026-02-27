package com.yaquodorg.yaquod.service.auth;

import com.yaquodorg.yaquod.dtos.LoginUserDto;
import com.yaquodorg.yaquod.dtos.RegisterUserDto;
import com.yaquodorg.yaquod.dtos.ResetPasswordDto;
import com.yaquodorg.yaquod.dtos.VehicleLoginDto;
import com.yaquodorg.yaquod.dtos.VerifyCodeDto;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.LoginResponse;
import com.yaquodorg.yaquod.response.VehicleLoginResponse;

public interface AuthenticationService {
    LoginResponse login(LoginUserDto loginUserDto);

    VehicleLoginResponse vehicleLogin(VehicleLoginDto vehicleLoginDto);

    User signup(RegisterUserDto registerUserDto, String role);

    LoginResponse refreshToken(String authHeader);

    boolean verifyUser(VerifyCodeDto verifyCodeDto);

    void regenerateOtp(String email);

    boolean resetPassword(ResetPasswordDto resetPasswordDto);
}
