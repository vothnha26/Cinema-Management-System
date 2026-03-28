package com.example.cinema.service.movie;

import java.util.Map;

public interface BuzzAnalysisService {
    Map<Long, Double> getExternalBuzzScores();
}
