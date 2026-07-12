package com.example.cinema.service.room;

import com.example.cinema.model.dto.request.RoomTemplateRequest;
import java.util.List;

public interface RoomTemplateService {
    void saveTemplate(RoomTemplateRequest request);
    RoomTemplateRequest getTemplate(String fileName);
    List<String> getAllTemplateFileNames();
    void deleteTemplate(String fileName);
}
