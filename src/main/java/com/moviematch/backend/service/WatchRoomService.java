package com.moviematch.backend.service;

import com.moviematch.backend.dto.PlaybackStateResponse;
import com.moviematch.backend.dto.WatchRoomMessage;
import com.moviematch.backend.dto.WatchRoomResponse;
import com.moviematch.backend.exception.ResourceAccessDeniedException;
import com.moviematch.backend.exception.ResourceNotFoundException;
import com.moviematch.backend.model.Match;
import com.moviematch.backend.model.WatchRoom;
import com.moviematch.backend.repository.MatchRepository;
import com.moviematch.backend.repository.UserRepository;
import com.moviematch.backend.repository.WatchRoomRepository;
import org.springframework.stereotype.Service;
import com.moviematch.backend.exception.DuplicateWatchRoomException;
import org.springframework.dao.DuplicateKeyException;
import java.time.LocalDateTime;

@Service
public class WatchRoomService {

    private final WatchRoomRepository watchRoomRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final PlaybackCacheService playbackCacheService;

    public WatchRoomService(
            WatchRoomRepository watchRoomRepository,
            MatchRepository matchRepository,
            UserRepository userRepository,
            PlaybackCacheService playbackCacheService) {

        this.watchRoomRepository = watchRoomRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.playbackCacheService = playbackCacheService;
    }

    public WatchRoomResponse createRoom(String userId, String matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Match not found"));

        verifyMatchMembership(match, userId);

        WatchRoom existingRoom = watchRoomRepository
                .findByMatchId(matchId)
                .orElse(null);

        if (existingRoom != null) {
            return mapToResponse(existingRoom);
        }

        WatchRoom room = new WatchRoom();
        room.setMatchId(matchId);
        room.setMovieId(match.getMovieId());
        room.setHostUserId(userId);
        room.setActive(true);
        room.setCreatedAt(LocalDateTime.now());

        try {
            WatchRoom savedRoom = watchRoomRepository.save(room);
            return mapToResponse(savedRoom);

        } catch (DuplicateKeyException exception) {

            WatchRoom concurrentlyCreatedRoom = watchRoomRepository
                    .findByMatchId(matchId)
                    .orElseThrow(() ->
                            new DuplicateWatchRoomException(
                                    "A watch room already exists for this match"));

            return mapToResponse(concurrentlyCreatedRoom);
        }
    }
    public WatchRoomResponse getRoom(String userId, String roomId) {

        WatchRoom room = watchRoomRepository
                .findByIdAndActiveTrue(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Watch room not found"));

        Match match = matchRepository.findById(room.getMatchId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Match not found"));

        verifyMatchMembership(match, userId);

        return mapToResponse(room);
    }

    public void verifyRoomAccess(String email, String roomId) {

        String userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"))
                .getId();

        WatchRoom room = watchRoomRepository
                .findByIdAndActiveTrue(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Watch room not found"));

        Match match = matchRepository.findById(room.getMatchId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Match not found"));

        verifyMatchMembership(match, userId);
    }

    public WatchRoom updatePlayback(
            String email,
            WatchRoomMessage message) {

        verifyRoomAccess(email, message.getRoomId());

        WatchRoom room = watchRoomRepository
                .findByIdAndActiveTrue(message.getRoomId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Watch room not found"));

        switch (message.getAction()) {
            case PLAY -> {
                room.setCurrentPosition(message.getPosition());
                room.setPlaying(true);
            }
            case PAUSE -> {
                room.setCurrentPosition(message.getPosition());
                room.setPlaying(false);
            }
            case SEEK ->
                    room.setCurrentPosition(message.getPosition());
        }

        room.setLastPlaybackUpdate(LocalDateTime.now());

        WatchRoom savedRoom = watchRoomRepository.save(room);

        PlaybackStateResponse playbackState = toPlaybackState(savedRoom);
        playbackCacheService.put(playbackState);

        return savedRoom;
    }

    public PlaybackStateResponse getPlaybackState(
            String email,
            String roomId) {

        verifyRoomAccess(email, roomId);

        PlaybackStateResponse cachedState = playbackCacheService.get(roomId);

        if (cachedState != null) {
            return cachedState;
        }

        WatchRoom room = watchRoomRepository
                .findByIdAndActiveTrue(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Watch room not found"));

        PlaybackStateResponse playbackState = toPlaybackState(room);

        playbackCacheService.put(playbackState);

        return playbackState;
    }

    private void verifyMatchMembership(Match match, String userId) {

        boolean isParticipant =
                match.getUser1Id().equals(userId)
                        || match.getUser2Id().equals(userId);

        if (!isParticipant) {
            throw new ResourceAccessDeniedException(
                    "You are not part of this match");
        }
    }

    private WatchRoomResponse mapToResponse(WatchRoom room) {

        return new WatchRoomResponse(
                room.getId(),
                room.getMatchId(),
                room.getMovieId(),
                room.getHostUserId(),
                room.isActive(),
                room.getCreatedAt()
        );
    }

    private PlaybackStateResponse toPlaybackState(WatchRoom room) {

        return new PlaybackStateResponse(
                room.getId(),
                room.getMovieId(),
                room.getCurrentPosition(),
                room.isPlaying(),
                room.getLastPlaybackUpdate()
        );
    }
}