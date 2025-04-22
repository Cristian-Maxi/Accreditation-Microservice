package com.microservice.accreditations.testControllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import com.microservice.accreditations.controllers.AccreditationController;
import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationRequestDTO;
import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationResponseDTO;
import com.microservice.accreditations.services.AccreditationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;


@WebMvcTest(AccreditationController.class)
public class AccreditationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccreditationService accreditationService;

    @InjectMocks
    private AccreditationController accreditationController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAccreditation_Success() throws Exception {
        AccreditationRequestDTO request = new AccreditationRequestDTO("cliente@ejemplo.com", 250.75, 3L);
        AccreditationResponseDTO response = new AccreditationResponseDTO(1L, 5L, 250.75, 3L, "Terminal Norte", LocalDateTime.now());

        when(accreditationService.saveAccreditation(any(AccreditationRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/accreditations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Authorities", "CLIENT")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value(true))
                .andExpect(jsonPath("$.message").value("Accreditation created successfully"))
                .andExpect(jsonPath("$.data.userId").value(5L))
                .andDo(print());
    }

    @Test
    public void testCreateAccreditation_AccessDenied() throws Exception {
        AccreditationRequestDTO request = new AccreditationRequestDTO("cliente@ejemplo.com", 250.75, 3L);

        mockMvc.perform(post("/api/accreditations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Authorities", "ADMIN")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: You must be a CLIENT to create an Accreditation"))
                .andDo(print());
    }

    @Test
    public void testGetAllAccreditations_Success() throws Exception {
        AccreditationResponseDTO response = new AccreditationResponseDTO(1L, 5L, 250.75, 3L, "Terminal Norte", LocalDateTime.now());

        when(accreditationService.getAllAccreditations()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/accreditations/getAll")
                        .header("X-User-Authorities", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(true))
                .andExpect(jsonPath("$.message").value("Accreditations FOUND"))
                .andExpect(jsonPath("$.dataIterable[0].userId").value(5L))
                .andDo(print());
    }

    @Test
    public void testGetAllAccreditations_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/accreditations/getAll")
                        .header("X-User-Authorities", "CLIENT"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: You must be an ADMIN to see all Accreditations in Database"))
                .andDo(print());
    }

    @Test
    public void testGetAllAccreditations_NotFound() throws Exception {
        when(accreditationService.getAllAccreditations()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/accreditations/getAll")
                        .header("X-User-Authorities", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(false))
                .andExpect(jsonPath("$.message").value("Accreditations NOT FOUND"))
                .andDo(print());
    }
}