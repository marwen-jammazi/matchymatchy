package com.moviematch.backend.service;

import com.moviematch.backend.dto.MatchResponse;
import com.moviematch.backend.model.Match;
import com.moviematch.backend.repository.MatchRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional
    public Match createMatch(
            String currentUserId,
            String matchedUserId,
            String movieId) {

        // 1. Validate input
        Assert.hasText(currentUserId, "Current user ID must not be empty");
        Assert.hasText(matchedUserId, "Matched user ID must not be empty");
        Assert.hasText(movieId, "Movie ID must not be empty");

        if (currentUserId.equals(matchedUserId)) {
            throw new IllegalArgumentException(
                    "A user cannot match with themselves");
        }

        // 2. Canonical ordering
        boolean isCurrentFirst =
                currentUserId.compareTo(matchedUserId) < 0;

        String user1Id = isCurrentFirst
                ? currentUserId
                : matchedUserId;

        String user2Id = isCurrentFirst
                ? matchedUserId
                : currentUserId;

        // 3. Return an existing match or create a new one
        return matchRepository
                .findByUser1IdAndUser2IdAndMovieId(
                        user1Id,
                        user2Id,
                        movieId)
                .orElseGet(() ->
                        createAndSaveMatch(user1Id, user2Id, movieId));
    }

    private Match createAndSaveMatch(
            String user1Id,
            String user2Id,
            String movieId) {

        try {
            Match match = new Match();

            match.setUser1Id(user1Id);
            match.setUser2Id(user2Id);
            match.setMovieId(movieId);
            match.setCreatedAt(LocalDateTime.now());

            return matchRepository.save(match);

        } catch (DuplicateKeyException exception) {

            // Another request may have created this match concurrently.
            return matchRepository
                    .findByUser1IdAndUser2IdAndMovieId(
                            user1Id,
                            user2Id,
                            movieId)
                    .orElseThrow(() -> exception);
        }
    }

    /**
     * Retrieves all matches belonging to a user.
     *
     * Kept for compatibility with existing callers.
     */
    @Transactional(readOnly = true)
    public List<MatchResponse> getUserMatches(String userId) {

        if (userId == null || userId.isBlank()) {
            return List.of();
        }

        List<Match> matches =
                matchRepository.findByUser1IdOrUser2Id(userId, userId);

        return matches.stream()
                .map(match -> mapToMatchResponse(match, userId))
                .toList();
    }

    /**
     * Retrieves a paginated list of matches belonging to a user.
     */
    @Transactional(readOnly = true)
    public Page<MatchResponse> getUserMatches(
            String userId,
            int page,
            int size) {

        // 1. Validate user ID
        Assert.hasText(userId, "User ID must not be empty");

        // 2. Validate pagination parameters
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50");
        }

        // 3. Create the pagination request
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 4. Retrieve only this user's matches
        Page<Match> matches =
                matchRepository.findByUser1IdOrUser2Id(
                        userId,
                        userId,
                        pageable
                );

        // 5. Convert entities to response DTOs
        return matches.map(
                match -> mapToMatchResponse(match, userId));
    }

    /**
     * Converts a Match entity into the response visible to a participant.
     */
    private MatchResponse mapToMatchResponse(
            Match match,
            String currentUserId) {

        String counterpartUserId =
                match.getUser1Id().equals(currentUserId)
                        ? match.getUser2Id()
                        : match.getUser1Id();

        return new MatchResponse(
                match.getId(),
                counterpartUserId,
                match.getMovieId(),
                match.getCreatedAt()
        );
    }
}