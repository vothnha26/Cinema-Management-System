package com.example.cinema.controller;

import com.example.cinema.model.dto.request.ComboRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.ComboResponse;
import com.example.cinema.service.ComboService;
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

    public ComboController(ComboService comboService) {
        this.comboService = comboService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComboResponse>>> getAllCombos() {
        return ResponseEntity.ok(ApiResponse.ok(comboService.getAllCombos()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComboResponse>> createCombo(@RequestBody @Valid ComboRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(comboService.createCombo(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComboResponse>> updateCombo(@PathVariable Long id, @RequestBody @Valid ComboRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(comboService.updateCombo(id, request)));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ComboResponse>> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(ApiResponse.ok(comboService.updateStock(id, quantity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCombo(@PathVariable Long id) {
        comboService.deleteCombo(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
