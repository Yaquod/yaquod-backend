package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createFailureResponse;
import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yaquodorg.yaquod.dtos.LoginUserDto;
import com.yaquodorg.yaquod.dtos.RegenerateCodeDto;
import com.yaquodorg.yaquod.dtos.RegisterUserDto;
import com.yaquodorg.yaquod.dtos.ResetPasswordDto;
import com.yaquodorg.yaquod.dtos.VerifyCodeDto;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.response.LoginResponse;
import com.yaquodorg.yaquod.response.MessageResponse;
import com.yaquodorg.yaquod.service.auth.AuthenticationService;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/admin/signup")
    public ResponseEntity<ApiResponse<User>> adminRegister(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto, "ADMIN");
            return ResponseEntity.status(CREATED)
                    .body(createSuccessResponse(registeredUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Failed to register admin user: " + e.getMessage()));
        }
    }

    @PostMapping("/client/signup")
    public ResponseEntity<ApiResponse<User>> studentRegister(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto, "CLIENT");
            return ResponseEntity.status(CREATED)
                    .body(createSuccessResponse(registeredUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Failed to register student user: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<MessageResponse>> verifyCode(@RequestBody VerifyCodeDto verifyCodeDto) {
        try {
            boolean success = authenticationService.verifyUser(verifyCodeDto);
            if (success) {
                return ResponseEntity
                        .ok(createSuccessResponse(new MessageResponse("Account Verified Successfully!")));
            } else {
                return ResponseEntity.badRequest()
                        .body(createFailureResponse("Verification Failed, Code Might Be Invalid Or Expired!"));
            }
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(createFailureResponse("User not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Internal Server Error: " + e.getMessage()));
        }
    }

    @PostMapping("/regenerate-code")
    public ResponseEntity<ApiResponse<MessageResponse>> regenerateOtp(
            @RequestBody RegenerateCodeDto regenerateCodeDto) {
        try {
            authenticationService.regenerateOtp(regenerateCodeDto.getEmail());
            return ResponseEntity.ok(createSuccessResponse(new MessageResponse("OTP regenerated successfully." +
                    " Check your email for the new OTP.")));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse("Internal Server Error: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginUserDto loginUserDto) {
        try {
            LoginResponse loginResponse = authenticationService.login(loginUserDto);
            return ResponseEntity.ok(createSuccessResponse(loginResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Failed to login: " + e.getMessage()));
        }
    }

    @GetMapping("/token-refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            LoginResponse loginResponse = authenticationService.refreshToken(authorizationHeader);
            if (loginResponse == null)
                throw new NullPointerException();
            return ResponseEntity.ok(createSuccessResponse(loginResponse));
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Bad Request: " + e.getMessage()));
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(UNAUTHORIZED)
                    .body(createFailureResponse("Refresh Token Expired: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse("Unexpected Error: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        try {
            boolean success = authenticationService.resetPassword(resetPasswordDto);
            if (success) {
                return ResponseEntity
                        .ok(createSuccessResponse(new MessageResponse("Password Reset Successfully!")));
            } else {
                return ResponseEntity.status(FORBIDDEN)
                        .body(createFailureResponse("Reset Failed," + " Code Might Be Invalid Or Expired!"));
            }
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(createFailureResponse("User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse("Internal Server Error: " + e.getMessage()));
        }
    }
}
