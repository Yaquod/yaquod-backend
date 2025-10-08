package com.yaquodorg.yaquod.service.user;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yaquodorg.yaquod.dtos.UpdateUserDto;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.repository.UserRepository;
import com.yaquodorg.yaquod.service.jwt.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public User saveUser(User user) {
        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());
        if (userOptional.isPresent()) {
            throw new IllegalStateException("Email Already Exists!");
        }

        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUser(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User getUserByJwt(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);
            if (jwtService.validateToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            }
            throw new RuntimeException("Token is not valid");
        }
        throw new RuntimeException("Token is invalid");
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUser(String authHeader, UpdateUserDto updateUserDto) throws ParseException {
        User user = getUserByJwt(authHeader);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        user.setFirstName(updateUserDto.getFirstName());
        user.setLastName(updateUserDto.getLastName());
        user.setDob(new Date(simpleDateFormat.parse(updateUserDto.getDob()).getTime()));

        return user;
    }

    @Override
    @Transactional
    public void updateUserPhoto(String authHeader, String url) {
        User user = getUserByJwt(authHeader);

        user.setImageUrl(url);
        log.info("Updated on {}", Thread.currentThread().getName());
    }

    @Override
    @Transactional
    public void updateFcmToken(String email, String fcmToken) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user not found"));
        user.setFirebaseToken(fcmToken);
    }
}
