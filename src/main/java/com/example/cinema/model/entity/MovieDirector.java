package com.example.cinema.model.entity;

import com.example.cinema.model.enums.DirectorRole;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "movie_directors")
@IdClass(MovieDirector.MovieDirectorId.class)
public class MovieDirector {

    @Id
    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Id
    @ManyToOne
    @JoinColumn(name = "director_id")
    private Director director;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DirectorRole role;

    public MovieDirector() {
    }

    public MovieDirector(Movie movie, Director director, DirectorRole role) {
        this.movie = movie;
        this.director = director;
        this.role = role;
    }

    public DirectorRole getRole() {
        return role;
    }

    public void setRole(DirectorRole role) {
        this.role = role;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Director getDirector() {
        return director;
    }

    public void setDirector(Director director) {
        this.director = director;
    }

    public static class MovieDirectorId implements Serializable {
        private Long movie;
        private Long director;

        public MovieDirectorId() {}
        public MovieDirectorId(Long movie, Long director) {
            this.movie = movie;
            this.director = director;
        }

        public Long getMovie() { return movie; }
        public void setMovie(Long movie) { this.movie = movie; }
        public Long getDirector() { return director; }
        public void setDirector(Long director) { this.director = director; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MovieDirectorId that = (MovieDirectorId) o;
            return Objects.equals(movie, that.movie) && Objects.equals(director, that.director);
        }

        @Override
        public int hashCode() {
            return Objects.hash(movie, director);
        }
    }
}