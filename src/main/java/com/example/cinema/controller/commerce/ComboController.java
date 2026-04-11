package com.example.cinema.controller.commerce;

import com.example.cinema.model.dto.request.ComboRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.ComboResponse;
import com.example.cinema.service.commerce.ComboService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/combos")
@CrossOrigin(origins = "*")
public class ComboController {

    private final ComboService comboService;
    private final com.example.cinema.service.commerce.ComboMediaService comboMediaService;

    public ComboController(ComboService comboService, com.example.cinema.service.commerce.ComboMediaService comboMediaService) {
        this.comboService = comboService;
        this.comboMediaService = comboMediaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComboResponse>>> getAllCombos() {
        return ResponseEntity.ok(ApiResponse.ok(comboService.getAllCombos()));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<ComboResponse>>> getCombosByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.ok(comboService.getCombosByBranch(branchId)));
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ComboResponse>> createCombo(
            @RequestPart("combo") @Valid ComboRequest request,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image) throws java.io.IOException {
        String imageUrl = comboMediaService.uploadComboImage(image);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(comboService.createCombo(request, imageUrl)));
    }

    @PutMapping(value = "/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ComboResponse>> updateCombo(
            @PathVariable Long id,
            @RequestPart("combo") @Valid ComboRequest request,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image) throws java.io.IOException {
        String imageUrl = comboMediaService.uploadComboImage(image);
        return ResponseEntity.ok(ApiResponse.ok(comboService.updateCombo(id, request, imageUrl)));
    }


    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ComboResponse>> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(ApiResponse.ok(comboService.updateStock(id, quantity)));
    }

    @PatchMapping("/branch/{branchId}/combo/{comboId}/stock")
    public ResponseEntity<ApiResponse<ComboResponse>> updateBranchStock(
            @PathVariable Long branchId, 
            @PathVariable Long comboId, 
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(ApiResponse.ok(comboService.updateBranchStock(branchId, comboId, quantity)));
    }

    @PatchMapping("/branch/{branchId}/combo/{comboId}/toggle-active")
    public ResponseEntity<ApiResponse<ComboResponse>> toggleBranchActive(
            @PathVariable Long branchId, 
            @PathVariable Long comboId, 
            @RequestParam Boolean active) {
        return ResponseEntity.ok(ApiResponse.ok(comboService.toggleBranchActive(branchId, comboId, active)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCombo(@PathVariable Long id) {
        comboService.deleteCombo(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
