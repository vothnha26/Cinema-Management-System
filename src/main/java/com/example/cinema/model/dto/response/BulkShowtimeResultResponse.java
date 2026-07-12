package com.example.cinema.model.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BulkShowtimeResultResponse {
    private List<ShowtimeResponse> success = new ArrayList<>();
    private List<ConflictDetail> conflicts = new ArrayList<>();

    public static class ConflictDetail {
        private LocalDateTime requestedStartTime;
        private String reason;
        private String conflictingMovie;
        private String conflictingTime;

        public ConflictDetail(LocalDateTime requestedStartTime, String reason, String conflictingMovie, String conflictingTime) {
            this.requestedStartTime = requestedStartTime;
            this.reason = reason;
            this.conflictingMovie = conflictingMovie;
            this.conflictingTime = conflictingTime;
        }

        // Getters
        public LocalDateTime getRequestedStartTime() { return requestedStartTime; }
        public String getReason() { return reason; }
        public String getConflictingMovie() { return conflictingMovie; }
        public String getConflictingTime() { return conflictingTime; }
    }

    // Getters and Setters
    public List<ShowtimeResponse> getSuccess() { return success; }
    public void setSuccess(List<ShowtimeResponse> success) { this.success = success; }
    public List<ConflictDetail> getConflicts() { return conflicts; }
    public void setConflicts(List<ConflictDetail> conflicts) { this.conflicts = conflicts; }
}
