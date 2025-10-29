package com.yaquodorg.yaquod.service.user;

import com.yaquodorg.yaquod.dtos.UpdateUserDto;
import com.yaquodorg.yaquod.entity.User;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);

    Optional<User> getUser(String email);

    User getUserByJwt(String authHeader);

    User getUserById(Long id);

    List<User> getUsers();

    User updateUser(String authHeader, UpdateUserDto updateUserDto) throws ParseException;

    void updateUserPhoto(String authHeader, String url);

    void updateFcmToken(String email, String fcmToken);
}
