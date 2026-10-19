package com.moviematch.backend.controller;

import com.moviematch.backend.dto.PlaybackStateResponse;
import com.moviematch.backend.dto.WatchRoomResponse;
import com.moviematch.backend.repository.UserRepository;
import com.moviematch.backend.service.WatchRoomService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/watch-rooms")
public class WatchRoomController {

    private final WatchRoomService watchRoomService;
    private final UserRepository userRepository;

    public WatchRoomController(
            WatchRoomService watchRoomService,
            UserRepository userRepository) {

        this.watchRoomService = watchRoomService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{matchId}")
    public WatchRoomResponse createRoom(
            @PathVariable String matchId,
            Authentication authentication) {

        String userId = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"))
                .getId();

        return watchRoomService.createRoom(userId, matchId);
    }

    @GetMapping("/{roomId}")
    public WatchRoomResponse getRoom(
            @PathVariable String roomId,
            Authentication authentication) {

        String userId = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"))
                .getId();

        return watchRoomService.getRoom(userId, roomId);
    }

    @GetMapping("/{roomId}/playback")
    public PlaybackStateResponse getPlaybackState(
            @PathVariable String roomId,
            Authentication authentication) {

        return watchRoomService.getPlaybackState(
                authentication.getName(),
                roomId
        );
    }
}