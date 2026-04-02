package com.example.cinema.model.dto.request;

import com.example.cinema.model.enums.MovieStatus;
<<<<<<< HEAD
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.Set;

public class MovieRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Thời lượng không được để trống")
    @Positive(message = "Thời lượng phải là số dương")
    private Integer duration;

    private LocalDate releaseDate;

    @NotNull(message = "Trạng thái không được để trống")
    private MovieStatus status;

    private com.example.cinema.model.enums.AgeRating ageRating;

    private String trailerUrl;

    private Integer priorityLevel;

    private Set<Long> genreIds;
    private Set<Long> actorIds;
    private Set<Long> directorIds;

    public MovieRequest() {}
=======
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
>>>>>>> feature/Customer

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
<<<<<<< HEAD

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public MovieStatus getStatus() { return status; }
    public void setStatus(MovieStatus status) { this.status = status; }

    public com.example.cinema.model.enums.AgeRating getAgeRating() { return ageRating; }
    public void setAgeRating(com.example.cinema.model.enums.AgeRating ageRating) { this.ageRating = ageRating; }

    public String getRating() { return ageRating != null ? ageRating.name() : null; }
    public void setRating(String rating) { 
        if (rating != null) this.ageRating = com.example.cinema.model.enums.AgeRating.valueOf(rating);
    }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }

    public Set<Long> getGenreIds() { return genreIds; }
    public void setGenreIds(Set<Long> genreIds) { this.genreIds = genreIds; }

    public Set<Long> getActorIds() { return actorIds; }
    public void setActorIds(Set<Long> actorIds) { this.actorIds = actorIds; }

    public Set<Long> getDirectorIds() { return directorIds; }
    public void setDirectorIds(Set<Long> directorIds) { this.directorIds = directorIds; }
=======
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
>>>>>>> feature/Customer
}
