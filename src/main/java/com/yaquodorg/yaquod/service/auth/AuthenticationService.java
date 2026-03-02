package com.yaquodorg.yaquod.service.auth;

import com.yaquodorg.yaquod.dtos.GoogleIdTokenRequest;
import com.yaquodorg.yaquod.dtos.LoginUserDto;
import com.yaquodorg.yaquod.dtos.RegisterUserDto;
import com.yaquodorg.yaquod.dtos.ResetPasswordDto;
import com.yaquodorg.yaquod.dtos.VehicleLoginDto;
import com.yaquodorg.yaquod.dtos.VerifyCodeDto;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.LoginResponse;
import com.yaquodorg.yaquod.response.VehicleLoginResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AuthenticationService {
  LoginResponse login(LoginUserDto loginUserDto);

  VehicleLoginResponse vehicleLogin(VehicleLoginDto vehicleLoginDto);

  User signup(RegisterUserDto registerUserDto, String role);

  LoginResponse refreshToken(String authHeader);

  boolean verifyUser(VerifyCodeDto verifyCodeDto);

  void regenerateOtp(String email);

  boolean resetPassword(ResetPasswordDto resetPasswordDto);

  /**
   * Authenticates a user using Google ID token from Flutter Google Sign-In. Creates a new user if
   * one doesn't exist with the Google email.
   *
   * @param request The Google ID token request containing the ID token and optional FCM token
   * @return LoginResponse with access and refresh tokens
   * @throws GeneralSecurityException if token verification fails due to security issues
   * @throws IOException if there's an I/O error during token verification
   */
  LoginResponse googleLogin(GoogleIdTokenRequest request)
      throws GeneralSecurityException, IOException;
}
