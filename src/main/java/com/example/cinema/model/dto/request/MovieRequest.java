package com.example.cinema.model.dto.request;

import com.example.cinema.model.enums.MovieStatus;
import java.time.LocalDate;
import java.util.List;

public class MovieRequest {
    private String title;
    private String description;
    private Integer duration;
    private String posterUrl;
    private String trailerUrl;
    private String ageRating;
    private MovieStatus status;
    private String director;
    private String actors;
    private LocalDate releaseDate;
    private Double rating;
    private Long tmdbId;
    private List<String> genres;
    private String directorAvatarUrl;
    private String actorAvatarUrls;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }

    public MovieStatus getStatus() { return status; }
    public void setStatus(MovieStatus status) { this.status = status; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Long getTmdbId() { return tmdbId; }
    public void setTmdbId(Long tmdbId) { this.tmdbId = tmdbId; }
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
    public String getDirectorAvatarUrl() { return directorAvatarUrl; }
    public void setDirectorAvatarUrl(String url) { this.directorAvatarUrl = url; }
    public String getActorAvatarUrls() { return actorAvatarUrls; }
    public void setActorAvatarUrls(String urls) { this.actorAvatarUrls = urls; }
}
