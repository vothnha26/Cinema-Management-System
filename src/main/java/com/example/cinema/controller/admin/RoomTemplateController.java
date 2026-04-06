package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.request.RoomTemplateRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.service.room.RoomTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/room-templates")
@PreAuthorize("hasRole('MANAGER')")
public class RoomTemplateController {

    private final RoomTemplateService roomTemplateService;

    public RoomTemplateController(RoomTemplateService roomTemplateService) {
        this.roomTemplateService = roomTemplateService;
    }

    @PostMapping
    public ApiResponse<String> saveTemplate(@RequestBody RoomTemplateRequest request) {
        roomTemplateService.saveTemplate(request);
        return ApiResponse.ok("Template saved successfully");
    }

    @GetMapping
    public ApiResponse<List<String>> listTemplates() {
        return ApiResponse.ok(roomTemplateService.getAllTemplateFileNames());
    }

    @GetMapping("/{fileName}")
    public ApiResponse<RoomTemplateRequest> getTemplate(@PathVariable String fileName) {
        return ApiResponse.ok(roomTemplateService.getTemplate(fileName));
    }

    @DeleteMapping("/{fileName}")
    public ApiResponse<String> deleteTemplate(@PathVariable String fileName) {
        roomTemplateService.deleteTemplate(fileName);
        return ApiResponse.ok("Template deleted successfully");
    }
}
