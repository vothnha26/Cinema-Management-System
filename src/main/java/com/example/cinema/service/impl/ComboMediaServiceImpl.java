package com.example.cinema.service.impl;

import com.example.cinema.service.CloudinaryService;
import com.example.cinema.service.ComboMediaService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class ComboMediaServiceImpl implements ComboMediaService {

    private final CloudinaryService cloudinaryService;

    public ComboMediaServiceImpl(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public String uploadComboImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        Map result = cloudinaryService.upload(file, "combos");
        return (String) result.get("secure_url");
    }
}
