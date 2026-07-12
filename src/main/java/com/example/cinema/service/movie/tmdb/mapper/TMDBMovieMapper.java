package com.example.cinema.service.movie.tmdb.mapper;

import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.enums.AgeRating;
import com.example.cinema.model.enums.MovieStatus;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class TMDBMovieMapper {
    public void mapToEntity(Map<String, Object> source, Movie target) {
        target.setTitle((String) source.get("title"));
        target.setDescription((String) source.get("overview"));
        target.setPosterUrl("https://image.tmdb.org/t/p/w500" + source.get("poster_path"));
        target.setReleaseDate(java.time.LocalDate.parse((String) source.get("release_date")));
        target.setRating(((Number) source.get("vote_average")).doubleValue());
        
        // Thay thế if-else bằng logic an toàn hoặc default values
        target.setStatus(MovieStatus.COMING);
        target.setAgeRating(mapAgeRating((Boolean) source.get("adult")));
    }

    private AgeRating mapAgeRating(Boolean adult) {
        if (adult == null) return AgeRating.P;
        return adult ? AgeRating.T18 : AgeRating.P;
    }
}
