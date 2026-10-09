package com.moviematch.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaybackStateResponse {

    private String roomId;

    private String movieId;

    private double position;

    private boolean playing;

    private LocalDateTime lastPlaybackUpdate;
}