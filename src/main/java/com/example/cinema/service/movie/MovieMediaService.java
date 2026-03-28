package com.example.cinema.service.movie;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface MovieMediaService {
    String uploadPoster(MultipartFile poster) throws IOException;
    // Có thể thêm deletePoster(String url) sau này
}
