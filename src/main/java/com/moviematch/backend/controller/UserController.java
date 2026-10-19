package com.moviematch.backend.controller;

import com.moviematch.backend.dto.UserRequest;
import com.moviematch.backend.dto.UserResponse;
import com.moviematch.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/me")
    public String getCurrentUser(Authentication authentication) {
        return "Logged in as: " + authentication.getName();
    }
}