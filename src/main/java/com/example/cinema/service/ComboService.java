package com.example.cinema.service;

import com.example.cinema.model.dto.request.ComboRequest;
import com.example.cinema.model.dto.response.ComboResponse;
import java.util.List;

public interface ComboService {
    List<ComboResponse> getAllCombos();
    ComboResponse createCombo(ComboRequest request);
    ComboResponse updateCombo(Long id, ComboRequest request);
    ComboResponse updateStock(Long id, Integer quantity);
    void deleteCombo(Long id);
}
