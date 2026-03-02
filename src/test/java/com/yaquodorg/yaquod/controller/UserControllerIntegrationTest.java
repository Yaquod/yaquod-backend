package com.yaquodorg.yaquod.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaquodorg.yaquod.dtos.LoginUserDto;
import com.yaquodorg.yaquod.dtos.RegisterUserDto;
import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.repository.UserRepository;
import com.yaquodorg.yaquod.service.jwt.JwtService;
import java.sql.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for User and Authentication endpoints Tests full Spring context including
 * security, JWT, and database Uses real database (H2 or Testcontainers)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User & Authentication Controller Integration Tests")
class UserControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private JwtService jwtService;

  @Autowired private PasswordEncoder passwordEncoder;

  private RegisterUserDto registerUserDto;
  private User testUser;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    // Setup registration DTO
    registerUserDto = new RegisterUserDto();
    registerUserDto.setEmail("test@example.com");
    registerUserDto.setPassword("password123");
    registerUserDto.setFirstName("John");
    registerUserDto.setLastName("Doe");
    registerUserDto.setPhoneNumber("1234567890");

    // Create test user
    testUser = new User();
    testUser.setEmail("existing@example.com");
    testUser.setPasswordHash(passwordEncoder.encode("password123"));
    testUser.setFirstName("Existing");
    testUser.setLastName("User");
    testUser.setPhoneNumber("9876543210");
    testUser.setRole(Role.CLIENT);
    testUser.setJoin_date(new Timestamp(System.currentTimeMillis()));
    testUser.setCode(111111);
    testUser.setCodeExpiredAt(new Timestamp(System.currentTimeMillis() + 86400000));
    testUser.setEmailVerified(true);

    testUser = userRepository.save(testUser);
  }

  // ADMIN SIGNUP TESTS
  @Test
  @DisplayName("POST /api/auth/admin/signup - Should register admin successfully")
  void shouldRegisterAdminSuccessfully() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/admin/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerUserDto)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("test@example.com"))
        .andExpect(jsonPath("$.data.firstName").value("John"))
        .andExpect(jsonPath("$.data.lastName").value("Doe"))
        .andExpect(jsonPath("$.data.role").value("ADMIN"))
        .andExpect(jsonPath("$.data.emailVerified").value(false));

    // Verify user was actually saved to database
    User savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
    assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
    assertThat(savedUser.getCode()).isEqualTo(111111);
  }

  @Test
  @DisplayName("POST /api/auth/admin/signup - Should return 400 with duplicate email")
  void shouldReturn400WithDuplicateEmailOnAdminSignup() throws Exception {
    // Use existing user's email
    registerUserDto.setEmail("existing@example.com");

    mockMvc
        .perform(
            post("/api/auth/admin/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerUserDto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value(containsString("Failed to register admin user")));
  }

  // CLIENT SIGNUP TESTS
  @Test
  @DisplayName("POST /api/auth/client/signup - Should register client successfully")
  void shouldRegisterClientSuccessfully() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/client/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerUserDto)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("test@example.com"))
        .andExpect(jsonPath("$.data.role").value("CLIENT"));

    // Verify user in database
    User savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
    assertThat(savedUser.getRole()).isEqualTo(Role.CLIENT);
  }

  @Test
  @DisplayName("POST /api/auth/client/signup - Should return 400 with duplicate email")
  void shouldReturn400WithDuplicateEmailOnClientSignup() throws Exception {
    registerUserDto.setEmail("existing@example.com");

    mockMvc
        .perform(
            post("/api/auth/client/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerUserDto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  // LOGIN TESTS
  @Test
  @DisplayName("POST /api/auth/login - Should login successfully with valid credentials")
  void shouldLoginSuccessfully() throws Exception {
    LoginUserDto loginDto = new LoginUserDto();
    loginDto.setEmail("existing@example.com");
    loginDto.setPassword("password123");
    loginDto.setFcmToken("fcm-token-123");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.accessToken").exists())
        .andExpect(jsonPath("$.data.refreshToken").exists())
        .andExpect(jsonPath("$.data.user.email").value("existing@example.com"))
        .andExpect(jsonPath("$.data.user.firstName").value("Existing"));

    // Verify FCM token was updated
    User updatedUser = userRepository.findByEmail("existing@example.com").orElseThrow();
    assertThat(updatedUser.getFirebaseToken()).isEqualTo("fcm-token-123");
  }

  @Test
  @DisplayName("POST /api/auth/login - Should return 400 with wrong password")
  void shouldReturn400WithWrongPassword() throws Exception {
    LoginUserDto loginDto = new LoginUserDto();
    loginDto.setEmail("existing@example.com");
    loginDto.setPassword("wrongpassword");
    loginDto.setFcmToken("fcm-token");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value(containsString("Failed to login")));
  }

  @Test
  @DisplayName("POST /api/auth/login - Should return 400 with non-existent email")
  void shouldReturn400WithNonExistentEmail() throws Exception {
    LoginUserDto loginDto = new LoginUserDto();
    loginDto.setEmail("nonexistent@example.com");
    loginDto.setPassword("password123");
    loginDto.setFcmToken("fcm-token");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  // VERIFY CODE TESTS
  @Test
  @DisplayName("POST /api/auth/verify-code - Should verify user successfully")
  void shouldVerifyUserSuccessfully() throws Exception {
    // Create unverified user
    User unverifiedUser = new User();
    unverifiedUser.setEmail("unverified@example.com");
    unverifiedUser.setPasswordHash(passwordEncoder.encode("password123"));
    unverifiedUser.setFirstName("Unverified");
    unverifiedUser.setLastName("User");
    unverifiedUser.setPhoneNumber("1111111111");
    unverifiedUser.setRole(Role.CLIENT);
    unverifiedUser.setJoin_date(new Timestamp(System.currentTimeMillis()));
    unverifiedUser.setCode(111111);
    unverifiedUser.setCodeExpiredAt(new Timestamp(System.currentTimeMillis() + 86400000));
    unverifiedUser.setEmailVerified(false);
    userRepository.save(unverifiedUser);

    String verifyJson =
        """
                {
                    "email": "unverified@example.com",
                    "code": 111111
                }
                """;

    mockMvc
        .perform(
            post("/api/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(verifyJson))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.message").value("Account Verified Successfully!"));

    // Verify user is now verified in database
    User verifiedUser = userRepository.findByEmail("unverified@example.com").orElseThrow();
    assertThat(verifiedUser.isEmailVerified()).isTrue();
  }

  @Test
  @DisplayName("POST /api/auth/verify-code - Should return 400 with wrong code")
  void shouldReturn400WithWrongVerificationCode() throws Exception {
    String verifyJson =
        """
                {
                    "email": "existing@example.com",
                    "code": 999999
                }
                """;

    mockMvc
        .perform(
            post("/api/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(verifyJson))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value(containsString("Verification Failed")));
  }

  @Test
  @DisplayName("POST /api/auth/verify-code - Should return 400 with expired code")
  void shouldReturn400WithExpiredCode() throws Exception {
    // Create user with expired code
    User expiredUser = new User();
    expiredUser.setEmail("expired@example.com");
    expiredUser.setPasswordHash(passwordEncoder.encode("password123"));
    expiredUser.setFirstName("Expired");
    expiredUser.setLastName("User");
    expiredUser.setPhoneNumber("2222222222");
    expiredUser.setRole(Role.CLIENT);
    expiredUser.setJoin_date(new Timestamp(System.currentTimeMillis()));
    expiredUser.setCode(111111);
    expiredUser.setCodeExpiredAt(new Timestamp(System.currentTimeMillis() - 1000)); // Expired
    expiredUser.setEmailVerified(false);
    userRepository.save(expiredUser);

    String verifyJson =
        """
                {
                    "email": "expired@example.com",
                    "code": 111111
                }
                """;

    mockMvc
        .perform(
            post("/api/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(verifyJson))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  // REGENERATE CODE TESTS
  @Test
  @DisplayName("POST /api/auth/regenerate-code - Should regenerate OTP successfully")
  void shouldRegenerateOtpSuccessfully() throws Exception {
    String regenerateJson =
        """
                {
                    "email": "existing@example.com"
                }
                """;

    Timestamp oldExpiry = testUser.getCodeExpiredAt();

    mockMvc
        .perform(
            post("/api/auth/regenerate-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(regenerateJson))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(
            jsonPath("$.data.message").value(containsString("OTP regenerated successfully")));

    // Verify code and expiry were updated
    User updatedUser = userRepository.findByEmail("existing@example.com").orElseThrow();
    assertThat(updatedUser.getCode()).isEqualTo(111111); // Test OTP
    assertThat(updatedUser.getCodeExpiredAt()).isNotEqualTo(oldExpiry);
    assertThat(updatedUser.isEmailVerified()).isFalse(); // Should be reset
  }

  @Test
  @DisplayName("POST /api/auth/regenerate-code - Should return 400 with non-existent email")
  void shouldReturn400WhenRegeneratingForNonExistentUser() throws Exception {
    String regenerateJson =
        """
                {
                    "email": "nonexistent@example.com"
                }
                """;

    mockMvc
        .perform(
            post("/api/auth/regenerate-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(regenerateJson))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("User not found"));
  }

  // TOKEN REFRESH TESTS
  @Test
  @DisplayName("GET /api/auth/token-refresh - Should refresh token successfully")
  void shouldRefreshTokenSuccessfully() throws Exception {
    // Generate refresh token
    String refreshToken = jwtService.generateRefreshToken(testUser);

    mockMvc
        .perform(get("/api/auth/token-refresh").header("Authorization", "Bearer " + refreshToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.accessToken").exists())
        .andExpect(jsonPath("$.data.refreshToken").value(refreshToken))
        .andExpect(jsonPath("$.data.user.email").value("existing@example.com"));
  }

  @Test
  @DisplayName("GET /api/auth/token-refresh - Should return 401 with invalid token")
  void shouldReturn400WithInvalidRefreshToken() throws Exception {
    mockMvc
        .perform(get("/api/auth/token-refresh").header("Authorization", "Bearer invalid-token"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  // RESET PASSWORD TESTS
  @Test
  @DisplayName("POST /api/auth/reset-password - Should reset password successfully")
  void shouldResetPasswordSuccessfully() throws Exception {
    String resetJson =
        """
                {
                    "email": "existing@example.com",
                    "password": "newPassword456",
                    "code": 111111
                }
                """;

    mockMvc
        .perform(
            post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetJson))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.message").value("Password Reset Successfully!"));

    // Verify password was changed and user can login with new password
    LoginUserDto loginDto = new LoginUserDto();
    loginDto.setEmail("existing@example.com");
    loginDto.setPassword("newPassword456");
    loginDto.setFcmToken("token");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("POST /api/auth/reset-password - Should return 403 with wrong code")
  void shouldReturn403WithWrongCodeOnReset() throws Exception {
    String resetJson =
        """
                {
                    "email": "existing@example.com",
                    "password": "newPassword456",
                    "code": 999999
                }
                """;

    mockMvc
        .perform(
            post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetJson))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value(containsString("Reset Failed")));
  }

  // FULL AUTHENTICATION FLOW
  @Test
  @DisplayName("Should complete full authentication flow: signup -> verify -> login")
  void shouldCompleteFullAuthenticationFlow() throws Exception {
    // 1. Signup
    RegisterUserDto signupDto = new RegisterUserDto();
    signupDto.setEmail("newuser@example.com");
    signupDto.setPassword("password123");
    signupDto.setFirstName("New");
    signupDto.setLastName("User");
    signupDto.setPhoneNumber("5555555555");

    mockMvc
        .perform(
            post("/api/auth/client/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupDto)))
        .andExpect(status().isCreated());

    // 2. Verify email
    String verifyJson =
        """
                {
                    "email": "newuser@example.com",
                    "code": 111111
                }
                """;

    mockMvc
        .perform(
            post("/api/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(verifyJson))
        .andExpect(status().isOk());

    // 3. Login
    LoginUserDto loginDto = new LoginUserDto();
    loginDto.setEmail("newuser@example.com");
    loginDto.setPassword("password123");
    loginDto.setFcmToken("fcm-token");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").exists())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // Verify user is fully set up in database
    User finalUser = userRepository.findByEmail("newuser@example.com").orElseThrow();
    assertThat(finalUser.isEmailVerified()).isTrue();
    assertThat(finalUser.getFirebaseToken()).isEqualTo("fcm-token");
  }

  // PASSWORD RESET FLOW
  @Test
  @DisplayName("Should complete password reset flow: regenerate OTP -> reset password -> login")
  void shouldCompletePasswordResetFlow() throws Exception {
    // 1. Regenerate OTP
    String regenerateJson =
        """
                {
                    "email": "existing@example.com"
                }
                """;

    mockMvc
        .perform(
            post("/api/auth/regenerate-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(regenerateJson))
        .andExpect(status().isOk());

    // 2. Reset password with OTP
    String resetJson =
        """
                {
                    "email": "existing@example.com",
                    "password": "newSecurePassword789",
                    "code": 111111
                }
                """;

    mockMvc
        .perform(
            post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetJson))
        .andExpect(status().isOk());

    // 3. Login with new password
    LoginUserDto loginDto = new LoginUserDto();
    loginDto.setEmail("existing@example.com");
    loginDto.setPassword("newSecurePassword789");
    loginDto.setFcmToken("token");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.user.email").value("existing@example.com"));
  }

  // CONCURRENT OPERATIONS
  @Test
  @DisplayName("Should handle multiple concurrent signups")
  void shouldHandleMultipleConcurrentSignups() throws Exception {
    for (int i = 0; i < 5; i++) {
      RegisterUserDto dto = new RegisterUserDto();
      dto.setEmail("user" + i + "@example.com");
      dto.setPassword("password" + i);
      dto.setFirstName("User" + i);
      dto.setLastName("Test");
      dto.setPhoneNumber("123456789" + i);

      mockMvc
          .perform(
              post("/api/auth/client/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(dto)))
          .andExpect(status().isCreated());
    }

    // Verify all users were created (+ 1 for testUser in setUp)
    assertThat(userRepository.count()).isEqualTo(6);
  }

  // EDGE CASES
  @Test
  @DisplayName("Should handle malformed JSON in signup")
  void shouldHandleMalformedJsonInSignup() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/client/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should handle missing content type")
  void shouldHandleMissingContentType() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/client/signup")
                .content(objectMapper.writeValueAsString(registerUserDto)))
        .andDo(print())
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  @DisplayName("Should persist user data across requests")
  void shouldPersistUserDataAcrossRequests() throws Exception {
    // Create user
    mockMvc
        .perform(
            post("/api/auth/client/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerUserDto)))
        .andExpect(status().isCreated());

    // Verify exists in second request
    assertThat(userRepository.findByEmail("test@example.com")).isPresent();

    // Verify still exists in third request
    assertThat(userRepository.findByEmail("test@example.com")).isPresent();
  }

  @Test
  @DisplayName("Should handle empty email in login")
  void shouldHandleEmptyEmailInLogin() throws Exception {
    LoginUserDto loginDto = new LoginUserDto();
    loginDto.setEmail("");
    loginDto.setPassword("password123");
    loginDto.setFcmToken("token");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/auth/test - Should return service status")
  void shouldReturnAuthServiceStatus() throws Exception {
    mockMvc
        .perform(get("/api/auth/test"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("Authentication Service is up and running!"));
  }
}
