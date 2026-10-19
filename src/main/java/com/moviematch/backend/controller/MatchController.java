package com.moviematch.backend.controller;

import com.moviematch.backend.dto.MatchResponse;
import com.moviematch.backend.dto.PageResponse;
import com.moviematch.backend.model.User;
import com.moviematch.backend.repository.UserRepository;
import com.moviematch.backend.service.MatchService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService matchService;
    private final UserRepository userRepository;

    public MatchController(
            MatchService matchService,
            UserRepository userRepository) {

        this.matchService = matchService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public PageResponse<MatchResponse> getMyMatches(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        ));

        Page<MatchResponse> matches =
                matchService.getUserMatches(
                        user.getId(),
                        page,
                        size
                );

        return new PageResponse<>(
                matches.getContent(),
                matches.getNumber(),
                matches.getSize(),
                matches.getTotalElements(),
                matches.getTotalPages(),
                matches.isLast()
        );
    }
}