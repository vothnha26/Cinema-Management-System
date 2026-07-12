package com.example.cinema.service.commerce;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ComboMediaService {
    String uploadComboImage(MultipartFile file) throws IOException;
}
