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

    List<ComboResponse> getCombosByBranch(Long branchId);

    ComboResponse updateBranchStock(Long branchId, Long comboId, Integer newStock);

    ComboResponse toggleBranchActive(Long branchId, Long comboId, Boolean active);
}
