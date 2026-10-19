package com.moviematch.backend.controller;

import com.moviematch.backend.dto.SwipeRequest;
import com.moviematch.backend.dto.SwipeResponse;
import com.moviematch.backend.service.SwipeService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/swipes")
public class SwipeController {

    private final SwipeService swipeService;

    public SwipeController(SwipeService swipeService) {
        this.swipeService = swipeService;
    }

    @PostMapping
    public SwipeResponse createSwipe(
            @Valid @RequestBody SwipeRequest request,
            Authentication authentication) {

        return swipeService.createSwipe(
                authentication.getName(),
                request
        );
    }
}