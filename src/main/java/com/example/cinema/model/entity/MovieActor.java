package com.example.cinema.model.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "movie_actors")
@IdClass(MovieActor.MovieActorId.class)
public class MovieActor {

    @Id
    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Id
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private Actor actor;

    public MovieActor() {
    }

    public MovieActor(Movie movie, Actor actor) {
        this.movie = movie;
        this.actor = actor;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public static class MovieActorId implements Serializable {
        private Long movie;
        private Long actor;

        public MovieActorId() {}
        public MovieActorId(Long movie, Long actor) {
            this.movie = movie;
            this.actor = actor;
        }

        public Long getMovie() { return movie; }
        public void setMovie(Long movie) { this.movie = movie; }
        public Long getActor() { return actor; }
        public void setActor(Long actor) { this.actor = actor; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MovieActorId that = (MovieActorId) o;
            return Objects.equals(movie, that.movie) && Objects.equals(actor, that.actor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(movie, actor);
        }
    }
}