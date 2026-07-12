package com.example.cinema.service.infrastructure.facade;

import com.example.cinema.service.infrastructure.CloudinaryService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Component
public class MediaFacade {
    private final CloudinaryService cloudinaryService;

    public MediaFacade(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    public String uploadMoviePoster(MultipartFile file) {
        try {
            Map result = cloudinaryService.upload(file, "movies");
            return result.get("secure_url").toString();
        } catch (Exception e) {
            return null;
        }
    }

    public String uploadComboImage(MultipartFile file) {
        try {
            Map result = cloudinaryService.upload(file, "combos");
            return result.get("secure_url").toString();
        } catch (Exception e) {
            return null;
        }
    }
}
