package com.example.cinema.service.commerce;

import com.example.cinema.model.dto.request.ComboRequest;
import com.example.cinema.model.dto.response.ComboResponse;

import java.util.List;

public interface ComboService {
    List<ComboResponse> getAllCombos();

    ComboResponse createCombo(ComboRequest request, String imageUrl);

    ComboResponse updateCombo(Long id, ComboRequest request, String imageUrl);

    ComboResponse updateStock(Long id, Integer quantity);

    void deleteCombo(Long id);
}
