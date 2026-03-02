package com.yaquodorg.yaquod.util;

import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.repository.UserRepository;
import java.sql.Timestamp;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class CustomSecurityContextFactory
    implements WithSecurityContextFactory<WithMockCustomUser> {

  private final UserRepository userRepository;

  public CustomSecurityContextFactory(ApplicationContext applicationContext) {
    this.userRepository = applicationContext.getBean(UserRepository.class);
  }

  @Override
  public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
    User user =
        userRepository
            .findByEmail(annotation.email())
            .orElseGet(
                () -> {
                  // Create user if not found
                  User newUser =
                      User.builder()
                          .email(annotation.email())
                          .passwordHash("password")
                          .firstName("Test")
                          .lastName("User")
                          .phoneNumber("+1234567890")
                          .join_date(new Timestamp(System.currentTimeMillis()))
                          .role(annotation.role()) // Use the role from annotation
                          .code(111111)
                          .emailVerified(true)
                          .build();
                  return userRepository.save(newUser);
                });

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    return context;
  }
}
