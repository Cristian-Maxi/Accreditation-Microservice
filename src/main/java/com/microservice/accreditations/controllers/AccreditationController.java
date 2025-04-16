package com.microservice.accreditations.controllers;

import com.microservice.accreditations.dtos.AccreditationsDTO.*;
import com.microservice.accreditations.dtos.ApiResponseDTO;
import com.microservice.accreditations.exceptions.ApplicationException;
import com.microservice.accreditations.services.AccreditationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accreditations")
public class AccreditationController {

    @Autowired
    private AccreditationService accreditationService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<AccreditationResponseDTO>> create(@RequestBody @Valid AccreditationRequestDTO request) {
        AccreditationResponseDTO responseDTO = accreditationService.saveAccreditation(request);
        return new ResponseEntity<>(new ApiResponseDTO<>(true, "Accreditation created successfully", responseDTO), HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponseDTO<AccreditationResponseDTO>> getAll() {
        try {
            List<AccreditationResponseDTO> allAccreditations = accreditationService.getAllAccreditations();
            if (allAccreditations.isEmpty()) {
                return new ResponseEntity<>(new ApiResponseDTO<>(false, "Accreditations NOT FOUND", allAccreditations), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ApiResponseDTO<>(true, "Accreditations FOUND", allAccreditations), HttpStatus.OK);
            }
        } catch (ApplicationException e) {
            throw new ApplicationException("An error has occurred: " + e.getMessage());
        }
    }
}
