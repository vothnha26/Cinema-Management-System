package com.example.cinema.model.dto.request;

import com.example.cinema.model.enums.MovieStatus;
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

    // Getters and Setters
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
}
