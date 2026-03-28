package com.example.cinema.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ComboMediaService {
    String uploadComboImage(MultipartFile file) throws IOException;
}
