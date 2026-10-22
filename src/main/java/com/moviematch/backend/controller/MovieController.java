package com.moviematch.backend.controller;

import com.moviematch.backend.dto.MovieResponse;
import com.moviematch.backend.service.MovieService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/{imdbId}")
    public MovieResponse getMovieById(@PathVariable String imdbId) {
        return movieService.getMovieById(imdbId);
    }
}