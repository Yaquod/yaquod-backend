package com.yaquodorg.yaquod.service.user;

import com.yaquodorg.yaquod.dtos.GoogleLoginDto;
import com.yaquodorg.yaquod.dtos.UpdateUserDto;
import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.User;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User saveUser(User user);

    User findOrCreateGoogleUser(GoogleLoginDto dto);

    Optional<User> getUser(String email);

    User getUserByJwt(String authHeader);

    User getUserById(Long id);

    User getUserByEmail(String email);

    List<User> getUsers();

    long countUsers();

    long countUsersByRole(Role role);

    User updateUser(String authHeader, UpdateUserDto updateUserDto) throws ParseException;

    void updateUserPhoto(String authHeader, String url);

    void updateFcmToken(String email, String fcmToken);

    Page<User> getAllUsers(Pageable pageable);

    Page<User> searchUsers(String query, Pageable pageable);

    User updateUserRole(Long userId, Role newRole);

    User updateEmailVerified(Long userId, boolean verified);
}
