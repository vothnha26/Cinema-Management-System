package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.MovieStatus;
import java.time.LocalDate;
import java.util.Set;

public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    private MovieStatus status;
    private String rating;
    private String posterUrl;
    private String trailerUrl;
    private Set<GenreResponse> genres;

    public static class GenreResponse {
        private Long id;
        private String name;
        // Getters/Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public MovieResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public MovieStatus getStatus() { return status; }
    public void setStatus(MovieStatus status) { this.status = status; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public Set<GenreResponse> getGenres() { return genres; }
    public void setGenres(Set<GenreResponse> genres) { this.genres = genres; }
}
