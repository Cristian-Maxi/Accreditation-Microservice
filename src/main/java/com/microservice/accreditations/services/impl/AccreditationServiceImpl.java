package com.microservice.accreditations.services.impl;

import com.microservice.accreditations.config.RabbitMQConfig;
import com.microservice.accreditations.dtos.AccreditationsDTO.*;
import com.microservice.accreditations.dtos.PointOfSaleDTO.PointOfSaleDTO;
import com.microservice.accreditations.exceptions.*;
import com.microservice.accreditations.mappers.AccreditationMapper;
import com.microservice.accreditations.models.Accreditation;
import com.microservice.accreditations.repositories.AccreditationRepository;
import com.microservice.accreditations.services.AccreditationService;
import com.microservice.accreditations.utils.AccreditationCache;
import com.microservice.accreditations.utils.AccreditationCreatedEvent;
import com.microservice.accreditations.utils.PointOfSaleRestTemplate;
import com.microservice.accreditations.utils.UserEntityRestTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AccreditationServiceImpl implements AccreditationService {

    private final AccreditationRepository accreditationRepository;
    private final PointOfSaleRestTemplate pointOfSaleRestTemplate;
    private final UserEntityRestTemplate userEntityRestTemplate;
    private final AccreditationMapper accreditationMapper;
    private final AccreditationCache accreditationCache;
    private final RabbitTemplate rabbitTemplate;

    public AccreditationServiceImpl(AccreditationRepository accreditationRepository, PointOfSaleRestTemplate pointOfSaleRestTemplate, UserEntityRestTemplate userEntityRestTemplate, AccreditationMapper accreditationMapper, AccreditationCache accreditationCache, RabbitTemplate rabbitTemplate) {
        this.accreditationRepository = accreditationRepository;
        this.pointOfSaleRestTemplate = pointOfSaleRestTemplate;
        this.userEntityRestTemplate = userEntityRestTemplate;
        this.accreditationMapper = accreditationMapper;
        this.accreditationCache = accreditationCache;
        this.rabbitTemplate = rabbitTemplate;
    }

    //@Transactional(rollbackFor = Exception.class)
    @Override
    public AccreditationResponseDTO saveAccreditation(AccreditationRequestDTO request) {
        PointOfSaleDTO pos = pointOfSaleRestTemplate.getPointOfSaleFromCacheOrHttp(request.pointOfSaleId());
        Long userId = userEntityRestTemplate.validateUserEmail(request.email());

        if (Objects.isNull(pos)) throw new PointOfSaleNotFoundException("Point of sale not found.");
        if (!pos.active()) throw new PointOfSaleInactiveException("Invalid or inactive point of sale.");

        Accreditation accreditation = accreditationMapper.toEntity(request);
        accreditation.setUserId(userId);
        accreditation.setPointOfSaleName(pos.name());
        accreditation.setReceivedAt(LocalDateTime.now());

        try {
            Accreditation saved = accreditationRepository.save(accreditation);
            AccreditationCreatedEvent event = accreditationMapper.toAccreditationCreatedEvent(saved);
            event.setEmail(request.email());

            System.out.println("Sending event to RabbitMQ: " + event);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);

            return accreditationMapper.toAccreditationResponseDTO(saved);
        } catch (Exception e) {
            throw new AccreditationSaveException("Failed to save accreditation: " + e.getMessage());
        }
    }

    @Override
    public List<AccreditationResponseDTO> getAllAccreditations() {
        List<AccreditationResponseDTO> cached = accreditationCache.getCachedAccreditations();

        if (Objects.nonNull(cached) && !cached.isEmpty()) {return cached;}

        List<AccreditationResponseDTO> fromDb = accreditationRepository.findAll().stream()
                .map(accreditationMapper::toAccreditationResponseDTO)
                .collect(Collectors.toList());

        if (fromDb.isEmpty()) {throw new AccreditationNotFoundException("No accreditations found.");}

        accreditationCache.cacheAccreditations(fromDb);
        return fromDb;
    }
}