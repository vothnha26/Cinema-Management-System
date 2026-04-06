package com.example.cinema.service.room.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.RoomTemplateRequest;
import com.example.cinema.service.room.RoomTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoomTemplateServiceImpl implements RoomTemplateService {

    private final String TEMPLATE_DIR = "data/room-templates";
    private final ObjectMapper objectMapper;
    private final com.example.cinema.repository.room.RoomTypeRepository roomTypeRepository;

    public RoomTemplateServiceImpl(ObjectMapper objectMapper, 
                                   com.example.cinema.repository.room.RoomTypeRepository roomTypeRepository) {
        this.objectMapper = objectMapper;
        this.roomTypeRepository = roomTypeRepository;
        // Đảm bảo thư mục tồn tại
        try {
            Files.createDirectories(Paths.get(TEMPLATE_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public void saveTemplate(RoomTemplateRequest request) {
        // Validation logic
        if (request.getRows() == null || request.getRows() <= 0 || request.getRows() > 26) {
            throw new AppException("Số hàng không hợp lệ (1-26)");
        }
        if (request.getCols() == null || request.getCols() <= 0 || request.getCols() > 30) {
            throw new AppException("Số cột không hợp lệ (1-30)");
        }
        if (!roomTypeRepository.existsById(request.getRoomTypeId())) {
            throw new AppException("Loại phòng không tồn tại: " + request.getRoomTypeId());
        }
        if (request.getSeats() == null || request.getSeats().isEmpty()) {
            throw new AppException("Template phải có ít nhất một ghế");
        }

        String fileName = String.format("%s_%s.json", 
                request.getRoomTypeId(), 
                request.getTemplateName().replaceAll("[^a-zA-Z0-9.-]", "_"));
        
        File file = new File(TEMPLATE_DIR, fileName);
        try {
            objectMapper.writeValue(file, request);
        } catch (IOException e) {
            throw new AppException("Lỗi khi lưu template file: " + e.getMessage());
        }
    }

    @Override
    public RoomTemplateRequest getTemplate(String fileName) {
        File file = new File(TEMPLATE_DIR, fileName);
        if (!file.exists()) {
            throw new AppException("Template không tồn tại: " + fileName);
        }
        try {
            return objectMapper.readValue(file, RoomTemplateRequest.class);
        } catch (IOException e) {
            throw new AppException("Lỗi khi đọc template file: " + e.getMessage());
        }
    }

    @Override
    public List<String> getAllTemplateFileNames() {
        try (Stream<Path> paths = Files.walk(Paths.get(TEMPLATE_DIR))) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".json"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new AppException("Lỗi khi liệt kê template: " + e.getMessage());
        }
    }

    @Override
    public void deleteTemplate(String fileName) {
        File file = new File(TEMPLATE_DIR, fileName);
        if (file.exists() && !file.delete()) {
            throw new AppException("Không thể xóa file template");
        }
    }
}
