package com.example.cinema.service.impl;

import com.example.cinema.service.CloudinaryService;
import com.example.cinema.service.MovieMediaService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class MovieMediaServiceImpl implements MovieMediaService {

    private final CloudinaryService cloudinaryService;

    public MovieMediaServiceImpl(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public String uploadPoster(MultipartFile poster) throws IOException {
        if (poster == null || poster.isEmpty()) return null;
        Map uploadResult = cloudinaryService.upload(poster, "movies");
        return (String) uploadResult.get("secure_url");
    }
}
