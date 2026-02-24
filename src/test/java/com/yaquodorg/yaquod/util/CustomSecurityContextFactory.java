package com.yaquodorg.yaquod.util;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.repository.UserRepository;

public class CustomSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    private final UserRepository userRepository;

    public CustomSecurityContextFactory(ApplicationContext applicationContext) {
        this.userRepository = applicationContext.getBean(UserRepository.class);
    }

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        User user = userRepository.findByEmail(annotation.email())
                .orElseThrow(() -> new IllegalStateException(
                        "Test user not found: " + annotation.email()));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
