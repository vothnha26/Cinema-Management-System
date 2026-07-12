package com.example.cinema.controller.movie;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.DirectorResponse;
import com.example.cinema.repository.movie.DirectorRepository;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/directors")
@CrossOrigin(origins = "*")
public class DirectorController {

    private final DirectorRepository directorRepository;
    private final ModelMapper modelMapper;

    public DirectorController(DirectorRepository directorRepository, ModelMapper modelMapper) {
        this.directorRepository = directorRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DirectorResponse>>> searchDirectors(@RequestParam String name) {
        List<DirectorResponse> directors = directorRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(director -> modelMapper.map(director, DirectorResponse.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(directors));
    }
}
