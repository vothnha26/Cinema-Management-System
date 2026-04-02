package com.example.cinema.model.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "movie_actors")
public class MovieActor {

    @EmbeddedId
    private MovieActorId id;

    @ManyToOne
    @MapsId("movieId")
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @MapsId("actorId")
    @JoinColumn(name = "actor_id")
    private Actor actor;

    @Column(name = "character_name")
    private String characterName;

    @Column(name = "display_order")
    private Integer displayOrder;

    public MovieActor() {}

    public MovieActor(MovieActorId id, Movie movie, Actor actor, String characterName, Integer displayOrder) {
        this.id = id;
        this.movie = movie;
        this.actor = actor;
        this.characterName = characterName;
        this.displayOrder = displayOrder;
    }

    public MovieActorId getId() { return id; }
    public void setId(MovieActorId id) { this.id = id; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public Actor getActor() { return actor; }
    public void setActor(Actor actor) { this.actor = actor; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    @Embeddable
    public static class MovieActorId implements Serializable {
        @Column(name = "movie_id")
        private Long movieId;

        @Column(name = "actor_id")
        private Long actorId;

        public MovieActorId() {}

        public MovieActorId(Long movieId, Long actorId) {
            this.movieId = movieId;
            this.actorId = actorId;
        }

        public Long getMovieId() { return movieId; }
        public void setMovieId(Long movieId) { this.movieId = movieId; }

        public Long getActorId() { return actorId; }
        public void setActorId(Long actorId) { this.actorId = actorId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MovieActorId that = (MovieActorId) o;
            return Objects.equals(movieId, that.movieId) && Objects.equals(actorId, that.actorId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(movieId, actorId);
        }
    }
}
