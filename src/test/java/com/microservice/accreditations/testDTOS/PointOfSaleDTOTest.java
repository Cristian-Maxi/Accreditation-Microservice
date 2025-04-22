package com.microservice.accreditations.testDTOS;

import com.microservice.accreditations.dtos.PointOfSaleDTO.PointOfSaleDTO;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PointOfSaleDTOTest {

    @Test
    public void testDTOConstruction() {
        PointOfSaleDTO dto = new PointOfSaleDTO(1L, "Terminal Norte", true);

        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals("Terminal Norte", dto.name());
        assertEquals(true, dto.active());
    }
}
