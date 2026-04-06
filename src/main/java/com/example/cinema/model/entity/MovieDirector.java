package com.example.cinema.model.entity;

import com.example.cinema.model.enums.DirectorRole;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "movie_directors")
public class MovieDirector {

    @EmbeddedId
    private MovieDirectorId id;

    @ManyToOne
    @MapsId("movieId")
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @MapsId("directorId")
    @JoinColumn(name = "director_id")
    private Director director;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DirectorRole role;

    public MovieDirector() {
    }

    public MovieDirector(MovieDirectorId id, Movie movie, Director director, DirectorRole role) {
        this.id = id;
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

    @Embeddable
    public static class MovieDirectorId implements Serializable {
        @Column(name = "movie_id")
        private Long movieId;

        @Column(name = "director_id")
        private Long directorId;

        public MovieDirectorId() {
        }

        public MovieDirectorId(Long movieId, Long directorId) {
            this.movieId = movieId;
            this.directorId = directorId;
        }

        public Long getMovieId() {
            return movieId;
        }

        public void setMovieId(Long movieId) {
            this.movieId = movieId;
        }

        public Long getDirectorId() {
            return directorId;
        }

        public void setDirectorId(Long directorId) {
            this.directorId = directorId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            MovieDirectorId that = (MovieDirectorId) o;
            return Objects.equals(movieId, that.movieId) && Objects.equals(directorId, that.directorId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(movieId, directorId);
        }
    }
}
