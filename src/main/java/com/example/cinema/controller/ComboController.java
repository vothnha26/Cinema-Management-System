package com.example.cinema.controller;

import com.example.cinema.model.dto.response.ComboResponse;
import com.example.cinema.repository.ComboRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/combos")
public class ComboController {

    private final ComboRepository comboRepository;

    public ComboController(ComboRepository comboRepository) {
        this.comboRepository = comboRepository;
    }

    @GetMapping
    public ResponseEntity<List<ComboResponse>> getActiveCombos() {
        List<ComboResponse> combos = comboRepository.findByIsActiveTrue()
                .stream()
                .map(combo -> {
                    ComboResponse response = new ComboResponse();
                    response.setId(combo.getId());
                    response.setName(combo.getName());
                    response.setDescription(combo.getDescription());
                    response.setPrice(combo.getPrice());
                    response.setImageUrl(combo.getImageUrl());
                    return response;
                })
                .toList();
        return ResponseEntity.ok(combos);
    }
}
