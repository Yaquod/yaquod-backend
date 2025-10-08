package com.yaquodorg.yaquod.config;

import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.repository.UserRepository;
import com.yaquodorg.yaquod.service.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        org.springframework.security.core.Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                                        newUser.setFirstName(givenName != null ? givenName : name);
                    newUser.setLastName(familyName != null ? familyName : "");
                    newUser.setRole(Role.CLIENT);
                    newUser.setPasswordHash("N/A");
                                        newUser.setPhoneNumber("N/A"); //Mobile number is required from the application
                    newUser.setJoin_date(Timestamp.valueOf(LocalDateTime.now()));
                    newUser.setEmailVerified(true);
                    return userRepository.save(newUser);
                });

        String token = jwtService.generateAccessToken(user);

        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + token + "\"}");
    }
}
