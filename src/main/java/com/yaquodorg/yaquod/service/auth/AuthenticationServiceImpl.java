package com.yaquodorg.yaquod.service.auth;

import java.sql.Timestamp;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yaquodorg.yaquod.dtos.LoginUserDto;
import com.yaquodorg.yaquod.dtos.RegisterUserDto;
import com.yaquodorg.yaquod.dtos.ResetPasswordDto;
import com.yaquodorg.yaquod.dtos.VerifyCodeDto;
import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.LoginResponse;
import com.yaquodorg.yaquod.service.jwt.JwtService;
import com.yaquodorg.yaquod.service.mail.MailSenderService;
import com.yaquodorg.yaquod.service.user.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final MailSenderService mailSenderService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponse login(LoginUserDto loginUserDto) {
        User authenticatedUser = authenticate(loginUserDto);
        userService.updateFcmToken(loginUserDto.getEmail(), loginUserDto.getFcmToken());

        String accessToken = jwtService.generateAccessToken(authenticatedUser);
        String refreshToken = jwtService.generateRefreshToken(authenticatedUser);

        return createLoginResponse(accessToken, refreshToken);
    }

    private User authenticate(LoginUserDto loginUserDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUserDto.getEmail(),
                        loginUserDto.getPassword()));

        return userService.getUser(loginUserDto.getEmail())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Override
    public User signup(RegisterUserDto registerUserDto, String role) {
        User user = createUserFromDto(registerUserDto);
        if (role.equals("ADMIN")) {
            user.setRole(Role.ADMIN);
        }

        String code = Integer.toString(user.getCode());
        User createdUser = userService.saveUser(user);
        mailSenderService.sendEmail(user.getEmail(), "Verification Code", code);

        return createdUser;
    }

    private User createUserFromDto(RegisterUserDto registerUserDto) {
        User user = new User();
        Date now = new Date();
        Date codeExpiryDate = new Date(now.getTime() + 86400000);

        user.setEmail(registerUserDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerUserDto.getPassword()));
        user.setFirstName(registerUserDto.getFirstName());
        user.setLastName(registerUserDto.getLastName());
        user.setPhoneNumber(registerUserDto.getPhoneNumber());
        user.setJoin_date(new Timestamp(now.getTime()));
        user.setCode(generateRandomOtp());
        user.setCodeExpiredAt(new Timestamp(codeExpiryDate.getTime()));
        user.setEmailVerified(false);

        return user;
    }

    private int generateRandomOtp() {
        int otpLength = 6;

        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;

        Random random = new Random();
        // return random.nextInt(max - min + 1) + min;
        // for testing environment, OTP is always 111111
        return 111111;
    }

    @Override
    public LoginResponse refreshToken(String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String refreshToken = authHeader.substring(7);
            if (jwtService.validateToken(refreshToken))
                return handleValidToken(refreshToken);
        }

        return null;
    }

    private LoginResponse handleValidToken(String refreshToken) {
        final String email = jwtService.getEmailFromToken(refreshToken);
        User user = userService.getUser(email).orElseThrow(() -> new NoSuchElementException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);

        return createLoginResponse(accessToken, refreshToken);
    }

    private LoginResponse createLoginResponse(String accessToken, String refreshToken) {
        LoginResponse loginResponse = new LoginResponse();
        String email = jwtService.getEmailFromToken(accessToken);
        User user = userService.getUser(email).orElseThrow(() -> new NoSuchElementException("User not found"));

        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setAccessTokenExpiresIn(jwtService.extractExpiration(accessToken));
        loginResponse.setRefreshTokenExpiresIn(jwtService.extractExpiration(refreshToken));
        loginResponse.setUser(user);

        return loginResponse;
    }

    @Override
    @Transactional
    public boolean verifyUser(VerifyCodeDto verifyCodeDto) {
        String email = verifyCodeDto.getEmail();
        int code = verifyCodeDto.getCode();

        User user = userService.getUser(email).orElseThrow(() -> new NoSuchElementException("User not found"));
        int verificationCode = user.getCode();

        if (code == verificationCode && isCodeValid(user.getCodeExpiredAt())) {
            user.setEmailVerified(true);
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public void regenerateOtp(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 86400000);
        User user = userService.getUser(email).orElseThrow(() -> new NoSuchElementException("User not found"));
        int newOtp = generateRandomOtp();

        user.setCode(newOtp);
        user.setCodeExpiredAt(new Timestamp(expiryDate.getTime()));
        user.setEmailVerified(false);

        String code = Integer.toString(user.getCode());
        mailSenderService.sendEmail(user.getEmail(), "Verification Code", code);
    }

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordDto resetPasswordDto) {
        String email = resetPasswordDto.getEmail();
        String newPassword = resetPasswordDto.getPassword();

        int code = resetPasswordDto.getCode();
        User user = userService.getUser(email).orElseThrow(() -> new NoSuchElementException("User not found"));
        int verificationCode = user.getCode();

        if (code == verificationCode && isCodeValid(user.getCodeExpiredAt())) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setEmailVerified(true);
            return true;
        }

        return false;
    }

    private boolean isCodeValid(Timestamp codeExpiredAt) {
        try {
            Date codeExpirationDate = new Date(codeExpiredAt.getTime());
            Date currentDate = new Date();
            return !currentDate.after(codeExpirationDate);
        } catch (Exception e) {
            // Default to code expired if parsing fails
            log.error("Error parsing code expiration date: {}", e.getMessage());
            return false;
        }
    }

}
