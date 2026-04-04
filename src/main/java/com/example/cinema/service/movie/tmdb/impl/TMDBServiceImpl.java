package com.example.cinema.service.movie.tmdb.impl;

import com.example.cinema.model.dto.tmdb.TMDBMovieDto;
import com.example.cinema.model.dto.tmdb.TMDBSearchResponse;
import com.example.cinema.service.movie.tmdb.TMDBService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TMDBServiceImpl implements TMDBService {

    private final RestTemplate restTemplate;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.image-base}")
    private String imageBase;

    private Map<Integer, String> genreCache = new java.util.HashMap<>();

    public TMDBServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        if (apiKey != null) {
            apiKey = apiKey.trim();
            loadMasterGenres();
        }
    }

    private void loadMasterGenres() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/genre/movie/list")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "vi")
                    .build().encode().toUriString();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("genres")) {
                List<Map<String, Object>> genres = (List<Map<String, Object>>) response.get("genres");
                for (Map<String, Object> g : genres) {
                    genreCache.put((Integer) g.get("id"), (String) g.get("name"));
                }
            }
        } catch (Exception e) {
            System.err.println(">>> TMDB: Không thể tải danh sách thể loại: " + e.getMessage());
        }
    }

    @Override
    public TMDBSearchResponse searchMovies(String query) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                    .queryParam("api_key", apiKey)
                    .queryParam("query", query)
                    .queryParam("language", "vi")
                    .build()
                    .encode()
                    .toUriString();

            TMDBSearchResponse response = restTemplate.getForObject(url, TMDBSearchResponse.class);
            if (response != null && response.getResults() != null) {
                response.getResults().forEach(m -> {
                    processPosters(m);
                    translateGenres(m);
                });
            }
            return response;
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi tìm kiếm TMDB: " + e.getMessage());
        }
    }

    @Override
    public TMDBMovieDto getMovieDetails(Long tmdbId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + tmdbId)
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "vi")
                    .queryParam("append_to_response", "credits,videos,release_dates")
                    .queryParam("include_video_language", "vi,en,null")
                    .build()
                    .encode()
                    .toUriString();

            TMDBMovieDto movie = restTemplate.getForObject(url, TMDBMovieDto.class);
            if (movie != null) {
                processPosters(movie);
                translateGenres(movie);
                processVNReleaseDate(movie);
            }
            return movie;
        } catch (Exception e) {
            System.err.println(">>> TMDB Detail Error: " + e.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi lấy chi tiết TMDB: " + e.getMessage());
        }
    }

    private void processVNReleaseDate(TMDBMovieDto movie) {
        if (movie == null || movie.getReleaseDates() == null || movie.getReleaseDates().getResults() == null) return;

        // 1. Tìm bản ghi tại Việt Nam (VN)
        Optional<TMDBMovieDto.ReleaseDateResult> vnRelease = movie.getReleaseDates().getResults().stream()
                .filter(r -> r != null && "VN".equalsIgnoreCase(r.getIso()))
                .findFirst();

        if (vnRelease.isPresent()) {
            List<TMDBMovieDto.ReleaseDateDetail> details = vnRelease.get().getReleaseDates();
            if (details != null) {
                // Lấy ngày phát hành đầu tiên có sẵn
                details.stream().filter(d -> d.getReleaseDate() != null).findFirst()
                        .ifPresent(d -> movie.setReleaseDate(d.getReleaseDate().split("T")[0]));
                
                // Lấy độ tuổi đầu tiên không trống
                details.stream()
                        .map(TMDBMovieDto.ReleaseDateDetail::getCertification)
                        .filter(c -> c != null && !c.isEmpty())
                        .findFirst()
                        .ifPresent(movie::setCertification);
            }
        }

        // 2. Nếu vẫn trống độ tuổi, lấy từ Mỹ (US) và dịch sang mác Việt Nam
        if (movie.getCertification() == null || movie.getCertification().isEmpty()) {
            movie.getReleaseDates().getResults().stream()
                .filter(r -> r != null && "US".equalsIgnoreCase(r.getIso()))
                .findFirst()
                .ifPresent(r -> {
                    if (r.getReleaseDates() != null) {
                        r.getReleaseDates().stream()
                            .map(TMDBMovieDto.ReleaseDateDetail::getCertification)
                            .filter(c -> c != null && !c.isEmpty())
                            .findFirst()
                            .ifPresent(usCert -> movie.setCertification(mapUSCertToVN(usCert)));
                    }
                });
        }
        
        // 3. Mặc định là P nếu không tìm thấy gì
        if (movie.getCertification() == null || movie.getCertification().isEmpty()) {
            movie.setCertification("P");
        }
    }

    private String mapUSCertToVN(String usCert) {
        if (usCert == null) return "P";
        switch (usCert.toUpperCase()) {
            case "G": return "P";
            case "PG": return "K";
            case "PG-13": return "T13";
            case "R": return "T18";
            case "NC-17": return "C18";
            default: return "P";
        }
    }

    private void translateGenres(TMDBMovieDto movie) {
        if (movie == null) return;
        if (genreCache.isEmpty()) loadMasterGenres();
        
        if (movie.getGenreIds() != null && !genreCache.isEmpty()) {
            List<TMDBMovieDto.Genre> genres = new ArrayList<>();
            for (Integer id : movie.getGenreIds()) {
                if (genreCache.containsKey(id)) {
                    TMDBMovieDto.Genre g = new TMDBMovieDto.Genre();
                    g.setId(id);
                    g.setName(genreCache.get(id));
                    genres.add(g);
                }
            }
            movie.setGenres(genres);
        } else if (movie.getGenres() != null && !genreCache.isEmpty()) {
            for (TMDBMovieDto.Genre g : movie.getGenres()) {
                if (g != null && genreCache.containsKey(g.getId())) {
                    g.setName(genreCache.get(g.getId()));
                }
            }
        }
    }

    private void processPosters(TMDBMovieDto movie) {
        if (movie == null) return;
        if (movie.getPosterPath() != null && !movie.getPosterPath().startsWith("http")) {
            movie.setPosterPath(imageBase + movie.getPosterPath());
        }
        if (movie.getBackdropPath() != null && !movie.getBackdropPath().startsWith("http")) {
            movie.setBackdropPath(imageBase + movie.getBackdropPath());
        }
    }
}
