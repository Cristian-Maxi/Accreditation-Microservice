package com.microservice.accreditations.testServices;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationRequestDTO;
import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationResponseDTO;
import com.microservice.accreditations.dtos.PointOfSaleDTO.PointOfSaleDTO;
import com.microservice.accreditations.exceptions.AccreditationNotFoundException;
import com.microservice.accreditations.exceptions.PointOfSaleInactiveException;
import com.microservice.accreditations.exceptions.PointOfSaleNotFoundException;
import com.microservice.accreditations.mappers.AccreditationMapper;
import com.microservice.accreditations.models.Accreditation;
import com.microservice.accreditations.repositories.AccreditationRepository;
import com.microservice.accreditations.services.impl.AccreditationServiceImpl;
import com.microservice.accreditations.utils.AccreditationCache;
import com.microservice.accreditations.utils.AccreditationCreatedEvent;
import com.microservice.accreditations.utils.PointOfSaleRestTemplate;
import com.microservice.accreditations.utils.UserEntityRestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class AccreditationServiceImplTest {

    @Mock
    private AccreditationRepository accreditationRepository;
    @Mock
    private PointOfSaleRestTemplate pointOfSaleRestTemplate;
    @Mock
    private UserEntityRestTemplate userEntityRestTemplate;
    @Mock
    private AccreditationMapper accreditationMapper;
    @Mock
    private AccreditationCache accreditationCache;
    @Mock
    private RabbitTemplate rabbitTemplate;

    AccreditationServiceImpl accreditationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        accreditationService = new AccreditationServiceImpl(accreditationRepository, pointOfSaleRestTemplate, userEntityRestTemplate, accreditationMapper, accreditationCache, rabbitTemplate);
    }

    @Test
    public void testSaveAccreditation_Success() {
        AccreditationRequestDTO request = new AccreditationRequestDTO("cliente@ejemplo.com", 250.75, 3L);
        PointOfSaleDTO pos = new PointOfSaleDTO(3L, "Terminal Norte", true);
        Accreditation accreditation = new Accreditation();
        accreditation.setUserId(5L);
        accreditation.setPointOfSaleName("Terminal Norte");
        accreditation.setReceivedAt(LocalDateTime.now());

        AccreditationCreatedEvent event = new AccreditationCreatedEvent();
        event.setEmail(request.email());

        when(pointOfSaleRestTemplate.getPointOfSaleFromCacheOrHttp(request.pointOfSaleId())).thenReturn(pos);
        when(userEntityRestTemplate.validateUserEmail(request.email())).thenReturn(5L);
        when(accreditationMapper.toEntity(request)).thenReturn(accreditation);
        when(accreditationRepository.save(accreditation)).thenReturn(accreditation);
        when(accreditationMapper.toAccreditationResponseDTO(accreditation)).thenReturn(new AccreditationResponseDTO(1L, 5L, 250.75, 3L, "Terminal Norte", LocalDateTime.now()));
        when(accreditationMapper.toAccreditationCreatedEvent(accreditation)).thenReturn(event);

        AccreditationResponseDTO response = accreditationService.saveAccreditation(request);

        assertNotNull(response);
        assertEquals(5L, response.userId());
        assertEquals("Terminal Norte", response.pointOfSaleName());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    public void testSaveAccreditation_PointOfSaleNotFound() {
        AccreditationRequestDTO request = new AccreditationRequestDTO("cliente@ejemplo.com", 250.75, 3L);

        when(pointOfSaleRestTemplate.getPointOfSaleFromCacheOrHttp(request.pointOfSaleId())).thenReturn(null);

        PointOfSaleNotFoundException exception = assertThrows(PointOfSaleNotFoundException.class, () -> {
            accreditationService.saveAccreditation(request);
        });

        assertEquals("Point of sale not found.", exception.getMessage());
    }

    @Test
    public void testSaveAccreditation_PointOfSaleInactive() {
        AccreditationRequestDTO request = new AccreditationRequestDTO("cliente@ejemplo.com", 250.75, 3L);
        PointOfSaleDTO pos = new PointOfSaleDTO(3L, "Terminal Norte", false);

        when(pointOfSaleRestTemplate.getPointOfSaleFromCacheOrHttp(request.pointOfSaleId())).thenReturn(pos);

        PointOfSaleInactiveException exception = assertThrows(PointOfSaleInactiveException.class, () -> {
            accreditationService.saveAccreditation(request);
        });

        assertEquals("Invalid or inactive point of sale.", exception.getMessage());
    }

    @Test
    public void testGetAllAccreditations_Cached() {
        AccreditationResponseDTO cachedAccreditation = new AccreditationResponseDTO(1L, 5L, 250.75, 3L, "Terminal Norte", LocalDateTime.now());
        when(accreditationCache.getCachedAccreditations()).thenReturn(List.of(cachedAccreditation));

        List<AccreditationResponseDTO> response = accreditationService.getAllAccreditations();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Terminal Norte", response.get(0).pointOfSaleName());
    }

    @Test
    public void testGetAllAccreditations_FromDb() {
        Accreditation accreditation = new Accreditation();
        accreditation.setUserId(5L);
        accreditation.setPointOfSaleName("Terminal Norte");
        accreditation.setReceivedAt(LocalDateTime.now());

        AccreditationResponseDTO responseDTO = new AccreditationResponseDTO(1L, 5L, 250.75, 3L, "Terminal Norte", LocalDateTime.now());

        when(accreditationCache.getCachedAccreditations()).thenReturn(Collections.emptyList());
        when(accreditationRepository.findAll()).thenReturn(List.of(accreditation));
        when(accreditationMapper.toAccreditationResponseDTO(accreditation)).thenReturn(responseDTO);

        List<AccreditationResponseDTO> response = accreditationService.getAllAccreditations();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Terminal Norte", response.get(0).pointOfSaleName());
        verify(accreditationCache, times(1)).cacheAccreditations(response);
    }

    @Test
    public void testGetAllAccreditations_NotFound() {
        when(accreditationCache.getCachedAccreditations()).thenReturn(Collections.emptyList());
        when(accreditationRepository.findAll()).thenReturn(Collections.emptyList());

        AccreditationNotFoundException exception = assertThrows(AccreditationNotFoundException.class, () -> {
            accreditationService.getAllAccreditations();
        });

        assertEquals("No accreditations found.", exception.getMessage());
    }
}
