package com.example.cinema.model.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TMDBMovieDto {
    private Long id;
    private String title;
    private String overview;
    
    @JsonProperty("poster_path")
    private String posterPath;
    
    @JsonProperty("backdrop_path")
    private String backdropPath;
    
    @JsonProperty("release_date")
    private String releaseDate;
    
    @JsonProperty("vote_average")
    private Double voteAverage;
    
    @JsonProperty("runtime")
    private Integer runtime;
    
    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    @JsonProperty("genres")
    private List<Genre> genres;

    public static class Genre {
        private Integer id;
        private String name;
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonProperty("credits")
    private Credits credits;

    @JsonProperty("videos")
    private Videos videos;

    @JsonProperty("release_dates")
    private ReleaseDatesWrapper releaseDates;

    @JsonProperty("keywords")
    private KeywordsWrapper keywords;

    private String certification;
    private List<String> suggestedFormats;

    public List<String> getSuggestedFormats() { return suggestedFormats; }
    public void setSuggestedFormats(List<String> suggestedFormats) { this.suggestedFormats = suggestedFormats; }

    public static class KeywordsWrapper {
        @JsonProperty("keywords")
        private List<Keyword> keywords;
        public List<Keyword> getKeywords() { return keywords; }
        public void setKeywords(List<Keyword> keywords) { this.keywords = keywords; }
    }

    public static class Keyword {
        private Integer id;
        private String name;
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class Credits {
        private List<Cast> cast;
        private List<Crew> crew;
        public List<Cast> getCast() { return cast; }
        public void setCast(List<Cast> cast) { this.cast = cast; }
        public List<Crew> getCrew() { return crew; }
        public void setCrew(List<Crew> crew) { this.crew = crew; }
    }

    public static class Cast {
        private String name;
        @JsonProperty("character")
        private String character;
        @JsonProperty("profile_path")
        private String profilePath;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCharacter() { return character; }
        public void setCharacter(String character) { this.character = character; }
        public String getProfilePath() { return profilePath; }
        public void setProfilePath(String profilePath) { this.profilePath = profilePath; }
    }

    public static class Crew {
        private String name;
        private String job;
        @JsonProperty("profile_path")
        private String profilePath;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getJob() { return job; }
        public void setJob(String job) { this.job = job; }
        public String getProfilePath() { return profilePath; }
        public void setProfilePath(String profilePath) { this.profilePath = profilePath; }
    }

    public static class Videos {
        private List<VideoResult> results;
        public List<VideoResult> getResults() { return results; }
        public void setResults(List<VideoResult> results) { this.results = results; }
    }

    public static class VideoResult {
        private String key;
        private String site;
        private String type;
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getSite() { return site; }
        public void setSite(String site) { this.site = site; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class ReleaseDatesWrapper {
        private List<ReleaseDateResult> results;
        public List<ReleaseDateResult> getResults() { return results; }
        public void setResults(List<ReleaseDateResult> results) { this.results = results; }
    }

    public static class ReleaseDateResult {
        @JsonProperty("iso_3166_1")
        private String iso;
        @JsonProperty("release_dates")
        private List<ReleaseDateDetail> releaseDates;
        public String getIso() { return iso; }
        public void setIso(String iso) { this.iso = iso; }
        public List<ReleaseDateDetail> getReleaseDates() { return releaseDates; }
        public void setReleaseDates(List<ReleaseDateDetail> releaseDates) { this.releaseDates = releaseDates; }
    }

    public static class ReleaseDateDetail {
        @JsonProperty("release_date")
        private String releaseDate;
        private String certification;
        private String note;
        private Integer type;

        public String getReleaseDate() { return releaseDate; }
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
        public String getCertification() { return certification; }
        public void setCertification(String certification) { this.certification = certification; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public Integer getType() { return type; }
        public void setType(Integer type) { this.type = type; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }
    public Integer getRuntime() { return runtime; }
    public void setRuntime(Integer runtime) { this.runtime = runtime; }
    public List<Integer> getGenreIds() { return genreIds; }
    public void setGenreIds(List<Integer> genreIds) { this.genreIds = genreIds; }
    public List<Genre> getGenres() { return genres; }
    public void setGenres(List<Genre> genres) { this.genres = genres; }
    public Credits getCredits() { return credits; }
    public void setCredits(Credits credits) { this.credits = credits; }
    public Videos getVideos() { return videos; }
    public void setVideos(Videos videos) { this.videos = videos; }
    public String getCertification() { return certification; }
    public void setCertification(String certification) { this.certification = certification; }
    public ReleaseDatesWrapper getReleaseDates() { return releaseDates; }
    public void setReleaseDates(ReleaseDatesWrapper releaseDates) { this.releaseDates = releaseDates; }

    public KeywordsWrapper getKeywords() { return keywords; }
    public void setKeywords(KeywordsWrapper keywords) { this.keywords = keywords; }
}
