package com.microservice.accreditations.controllers;

import com.microservice.accreditations.dtos.AccreditationsDTO.*;
import com.microservice.accreditations.dtos.ApiResponseDTO;
import com.microservice.accreditations.enums.RoleEnum;
import com.microservice.accreditations.exceptions.ApplicationException;
import com.microservice.accreditations.services.AccreditationService;
import com.microservice.accreditations.utils.RoleValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accreditations")
@Tag(name = "Accreditation", description = "Endpoints for managing Accreditations")
public class AccreditationController {

    private final AccreditationService accreditationService;

    public AccreditationController(AccreditationService accreditationService) {
        this.accreditationService = accreditationService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create Accreditation", description = "Creates a new Accreditation. Only CLIENT role is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Accreditation created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid request")})
    public ResponseEntity<ApiResponseDTO<AccreditationResponseDTO>> create(@RequestBody @Valid AccreditationRequestDTO request,
                                                                           @RequestHeader("X-User-Authorities") String roles) {
        RoleValidator.validateRole(roles,"Access denied: You must be a CLIENT to create an Accreditation", RoleEnum.CLIENT);
        AccreditationResponseDTO responseDTO = accreditationService.saveAccreditation(request);
        return new ResponseEntity<>(new ApiResponseDTO<>(true, "Accreditation created successfully", responseDTO), HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    @Operation(summary = "Get All Accreditations", description = "Retrieves all Accreditations in the system. Only ADMIN role is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accreditations retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<ApiResponseDTO<AccreditationResponseDTO>> getAll(@RequestHeader("X-User-Authorities") String roles) {
        RoleValidator.validateRole(roles,"Access denied: You must be an ADMIN to see all Accreditations in Database", RoleEnum.ADMIN);
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
