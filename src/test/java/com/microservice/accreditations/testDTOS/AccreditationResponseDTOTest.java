package com.microservice.accreditations.testDTOS;

import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationResponseDTO;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AccreditationResponseDTOTest {

    @Test
    public void testDTOConstruction() {
        LocalDateTime timestamp = LocalDateTime.of(2024, 7, 10, 14, 30);
        AccreditationResponseDTO dto = new AccreditationResponseDTO(
                1001L,
                5L,
                1500.00,
                2L,
                "Sucursal Centro",
                timestamp
        );

        assertNotNull(dto);
        assertEquals(1001L, dto.id());
        assertEquals(5L, dto.userId());
        assertEquals(1500.00, dto.amount());
        assertEquals(2L, dto.pointOfSaleId());
        assertEquals("Sucursal Centro", dto.pointOfSaleName());
        assertEquals(timestamp, dto.receivedAt());
    }
}
