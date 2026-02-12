package com.yaquodorg.yaquod.controller;

import com.yaquodorg.yaquod.dtos.*;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.response.LoginResponse;
import com.yaquodorg.yaquod.response.MessageResponse;
import com.yaquodorg.yaquod.service.auth.AuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

import static com.yaquodorg.yaquod.response.ApiResponse.createFailureResponse;
import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and user registration APIs")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register an admin user", description = "Creates a new admin user account with the provided registration details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Admin user registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid registration data or registration failed")
    })
    @PostMapping("/admin/signup")
    public ResponseEntity<ApiResponse<User>> adminRegister(@Valid @RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto, "ADMIN");
            return ResponseEntity.status(CREATED)
                    .body(createSuccessResponse(registeredUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Failed to register admin user: " + e.getMessage()));
        }
    }

    @Operation(summary = "Register a client user", description = "Creates a new client user account with the provided registration details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Client user registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid registration data or registration failed")
    })
    @PostMapping("/client/signup")
    public ResponseEntity<ApiResponse<User>> studentRegister(@Valid @RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto, "CLIENT");
            return ResponseEntity.status(CREATED)
                    .body(createSuccessResponse(registeredUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Failed to register student user: " + e.getMessage()));
        }
    }

    @Operation(summary = "Verify user account", description = "Verifies a user account using the verification code sent to their email")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired verification code")
    })
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<MessageResponse>> verifyCode(@Valid @RequestBody VerifyCodeDto verifyCodeDto) {
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

    @Operation(summary = "Regenerate OTP code", description = "Generates and sends a new OTP verification code to the user's email")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP regenerated and sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/regenerate-code")
    public ResponseEntity<ApiResponse<MessageResponse>> regenerateOtp(
            @Valid @RequestBody RegenerateCodeDto regenerateCodeDto) {
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

    @Operation(summary = "User login", description = "Authenticates a user and returns access and refresh tokens")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful, tokens returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid credentials or login failed")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginUserDto loginUserDto) {
        try {
            LoginResponse loginResponse = authenticationService.login(loginUserDto);
            return ResponseEntity.ok(createSuccessResponse(loginResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Failed to login: " + e.getMessage()));
        }
    }

    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or missing authorization header"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token expired"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/token-refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Parameter(description = "Bearer refresh token", required = true)
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            LoginResponse loginResponse = authenticationService.refreshToken(authorizationHeader);
            if (loginResponse == null)
                throw new IllegalArgumentException("Failed to refresh token: No authorization header provided");
            return ResponseEntity.ok(createSuccessResponse(loginResponse));
        } catch (IllegalArgumentException e) {
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

    @Operation(summary = "Reset password", description = "Resets user password using a valid verification code")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Invalid or expired reset code"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
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

    @Operation(summary = "Health check", description = "Simple endpoint to verify the authentication service is running")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/test")
    public String test() {
        return "Authentication Service is up and running!";
    }
}
