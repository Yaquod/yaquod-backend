package com.yaquodorg.yaquod.config;

import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.repository.UserRepository;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // NOTE:
        // TODO:
        // This should not be left in production for security.
        String email = "admin1@gmail.com";
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Admin user already exists: {}", email);
            return;
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode("admin1password"));
        admin.setFirstName("Admin");
        admin.setLastName("1");
        admin.setPhoneNumber("+201010149602");
        admin.setRole(Role.ADMIN);
        admin.setEmailVerified(true);
        admin.setJoin_date(new Timestamp(System.currentTimeMillis()));

        userRepository.save(admin);
        log.info("Default admin user created: {}", email);
    }
}
