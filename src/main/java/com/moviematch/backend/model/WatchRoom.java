package com.moviematch.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "watch_rooms")
public class WatchRoom {

    @Id
    private String id;

    @Indexed(unique = true)
    private String matchId;

    private String movieId;

    private String hostUserId;

    private boolean active;

    private LocalDateTime createdAt;

    // Playback state
    private double currentPosition;

    private boolean playing;

    private LocalDateTime lastPlaybackUpdate;
}