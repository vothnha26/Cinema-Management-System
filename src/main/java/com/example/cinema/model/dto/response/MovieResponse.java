package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.MovieStatus;
<<<<<<< HEAD
import java.time.LocalDate;
import java.util.Set;
=======
import java.util.List;
>>>>>>> feature/Customer

public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
<<<<<<< HEAD
    private LocalDate releaseDate;
    private MovieStatus status;
    private com.example.cinema.model.enums.AgeRating ageRating;
    private String posterUrl;
    private String trailerUrl;
    private Integer priorityLevel;
    private Set<GenreResponse> genres;
    private Set<ActorResponse> actors;
    private Set<DirectorResponse> directors;

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

    public com.example.cinema.model.enums.AgeRating getAgeRating() { return ageRating; }
    public void setAgeRating(com.example.cinema.model.enums.AgeRating ageRating) { this.ageRating = ageRating; }

    public String getRating() { return ageRating != null ? ageRating.name() : null; }
    public void setRating(String rating) { 
        if (rating != null) this.ageRating = com.example.cinema.model.enums.AgeRating.valueOf(rating);
    }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }

    public Set<GenreResponse> getGenres() { return genres; }
    public void setGenres(Set<GenreResponse> genres) { this.genres = genres; }

    public Set<ActorResponse> getActors() { return actors; }
    public void setActors(Set<ActorResponse> actors) { this.actors = actors; }

    public Set<DirectorResponse> getDirectors() { return directors; }
    public void setDirectors(Set<DirectorResponse> directors) { this.directors = directors; }
=======
    private String language;
    private String ageRating;
    private MovieStatus status;
    private String posterUrl;
    private String trailerUrl;
    private Double rating;
    private java.time.LocalDate releaseDate;
    
    // We will extract genres as list of strings
    private List<String> genres;
    private List<PersonResponse> actors;
    private List<PersonResponse> directors;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }
    public MovieStatus getStatus() { return status; }
    public void setStatus(MovieStatus status) { this.status = status; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public java.time.LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(java.time.LocalDate date) { this.releaseDate = date; }
    public List<PersonResponse> getActors() { return actors; }
    public void setActors(List<PersonResponse> actors) { this.actors = actors; }
    public List<PersonResponse> getDirectors() { return directors; }
    public void setDirectors(List<PersonResponse> directors) { this.directors = directors; }
>>>>>>> feature/Customer
}
