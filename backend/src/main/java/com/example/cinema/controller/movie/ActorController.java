package com.example.cinema.controller.movie;

import com.example.cinema.model.dto.response.ActorResponse;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.repository.movie.ActorRepository;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/actors")
@CrossOrigin(origins = "*")
public class ActorController {

    private final ActorRepository actorRepository;
    private final ModelMapper modelMapper;

    public ActorController(ActorRepository actorRepository, ModelMapper modelMapper) {
        this.actorRepository = actorRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ActorResponse>>> searchActors(@RequestParam String name) {
        List<ActorResponse> actors = actorRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(actor -> modelMapper.map(actor, ActorResponse.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(actors));
    }
}
