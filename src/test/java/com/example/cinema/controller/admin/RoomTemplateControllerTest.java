package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.request.RoomTemplateRequest;
import com.example.cinema.service.room.RoomTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomTemplateService roomTemplateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testSaveTemplate_Success() throws Exception {
        RoomTemplateRequest request = new RoomTemplateRequest();
        request.setTemplateName("Test Template");
        request.setRoomTypeId("STANDARD");
        request.setRows(10);
        request.setCols(10);
        request.setSeats(Arrays.asList(new RoomTemplateRequest.SeatTemplateDTO()));

        mockMvc.perform(post("/api/admin/room-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Template saved successfully"));

        Mockito.verify(roomTemplateService, Mockito.times(1)).saveTemplate(Mockito.any(RoomTemplateRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testListTemplates_Success() throws Exception {
        List<String> fileNames = Arrays.asList("STANDARD_Template1.json", "VIP_Template2.json");
        Mockito.when(roomTemplateService.getAllTemplateFileNames()).thenReturn(fileNames);

        mockMvc.perform(get("/api/admin/room-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0]").value("STANDARD_Template1.json"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    public void testSaveTemplate_ForbiddenForStaff() throws Exception {
        RoomTemplateRequest request = new RoomTemplateRequest();
        
        mockMvc.perform(post("/api/admin/room-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testDeleteTemplate_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/room-templates/test_file.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Template deleted successfully"));

        Mockito.verify(roomTemplateService, Mockito.times(1)).deleteTemplate("test_file.json");
    }
}
