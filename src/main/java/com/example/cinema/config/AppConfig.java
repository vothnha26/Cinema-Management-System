package com.example.cinema.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        
        // Tránh xung đột mapping tự động
        modelMapper.getConfiguration().setAmbiguityIgnored(true);

        // Converter: MovieActor -> ActorResponse
        modelMapper.typeMap(com.example.cinema.model.entity.MovieActor.class, com.example.cinema.model.dto.response.ActorResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getActor().getId(), com.example.cinema.model.dto.response.ActorResponse::setId);
            mapper.map(src -> src.getActor().getName(), com.example.cinema.model.dto.response.ActorResponse::setName);
        });

        // Converter: MovieDirector -> DirectorResponse
        modelMapper.typeMap(com.example.cinema.model.entity.MovieDirector.class, com.example.cinema.model.dto.response.DirectorResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getDirector().getId(), com.example.cinema.model.dto.response.DirectorResponse::setId);
            mapper.map(src -> src.getDirector().getName(), com.example.cinema.model.dto.response.DirectorResponse::setName);
        });

        // Map Movie -> MovieResponse
        modelMapper.typeMap(com.example.cinema.model.entity.Movie.class, com.example.cinema.model.dto.response.MovieResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getGenres(), com.example.cinema.model.dto.response.MovieResponse::setGenres);
            mapper.map(src -> src.getMovieActors(), com.example.cinema.model.dto.response.MovieResponse::setActors);
            mapper.map(src -> src.getMovieDirectors(), com.example.cinema.model.dto.response.MovieResponse::setDirectors);
        });

        // Map Showtime -> ShowtimeResponse
        modelMapper.typeMap(com.example.cinema.model.entity.Showtime.class, com.example.cinema.model.dto.response.ShowtimeResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getMovie().getId(), com.example.cinema.model.dto.response.ShowtimeResponse::setMovieId);
            mapper.map(src -> src.getMovie().getTitle(), com.example.cinema.model.dto.response.ShowtimeResponse::setMovieTitle);
            mapper.map(src -> src.getRoom().getId(), com.example.cinema.model.dto.response.ShowtimeResponse::setRoomId);
            mapper.map(src -> src.getRoom().getName(), com.example.cinema.model.dto.response.ShowtimeResponse::setRoomName);
        });

        return modelMapper;
    }

    @Bean
    public org.springframework.web.client.RestTemplate restTemplate() {
        return new org.springframework.web.client.RestTemplate();
    }

    @Bean
    public org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.simpleDateFormat("yyyy-MM-dd");
            builder.serializers(new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        };
    }
}
