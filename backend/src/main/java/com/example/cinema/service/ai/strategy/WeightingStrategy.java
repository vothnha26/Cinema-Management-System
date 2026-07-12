package com.example.cinema.service.ai.strategy;

import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.entity.Movie;
import java.time.LocalTime;
import java.util.Map;

public interface WeightingStrategy {
    /**
     * Tính toán trọng số bổ sung hoặc điều chỉnh cho một bộ phim.
     * 
     * @param movie Bộ phim đang xét
     * @param time Thời điểm dự kiến chiếu
     * @param buzzScores Map chứa điểm buzz của các phim
     * @param request Yêu cầu lập lịch từ người dùng
     * @param usageCount Số lần phim đã được lập lịch trong ngày
     * @param branchPriorities Độ ưu tiên của phim tại chi nhánh
     * @return Trọng số (có thể là điểm cộng dồn hoặc hệ số nhân)
     */
    double calculateWeight(Movie movie, LocalTime time, Map<Long, Double> buzzScores, 
                          SchedulingRequest request, Map<Long, Integer> usageCount, 
                          Map<Long, Integer> branchPriorities);
    
    /**
     * Thứ tự ưu tiên áp dụng (nếu cần thiết).
     */
    default int getOrder() { return 0; }
}
