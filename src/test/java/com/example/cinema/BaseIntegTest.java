package com.example.cinema;

import com.example.cinema.config.TestDotenvInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDotenvInitializer.class)
@Transactional
public abstract class BaseIntegTest {
    // Để cho các lớp con tự định nghĩa @AutoConfigureMockMvc nếu cần
}
