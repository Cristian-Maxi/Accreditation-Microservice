package com.microservice.accreditations.services.impl;

import com.microservice.accreditations.config.RabbitMQConfig;
import com.microservice.accreditations.dtos.AccreditationsDTO.*;
import com.microservice.accreditations.dtos.PointOfSaleDTO.PointOfSaleDTO;
import com.microservice.accreditations.exceptions.ApplicationException;
import com.microservice.accreditations.mappers.AccreditationMapper;
import com.microservice.accreditations.models.Accreditation;
import com.microservice.accreditations.repositories.AccreditationRepository;
import com.microservice.accreditations.services.AccreditationService;
import com.microservice.accreditations.utils.AccreditationCache;
import com.microservice.accreditations.utils.AccreditationCreatedEvent;
import com.microservice.accreditations.utils.PointOfSaleRestTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccreditationServiceImpl implements AccreditationService {
    @Autowired
    private AccreditationRepository accreditationRepository;
    @Autowired
    private PointOfSaleRestTemplate pointOfSaleRestTemplate;
    @Autowired
    private AccreditationMapper accreditationMapper;
    @Autowired
    private AccreditationCache accreditationCache;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public AccreditationResponseDTO saveAccreditation(AccreditationRequestDTO request) {
        PointOfSaleDTO pos = pointOfSaleRestTemplate.getPointOfSaleFromCacheOrHttp(request.pointOfSaleId());

        if (pos == null || !pos.active()) {
            throw new ApplicationException("Invalid or inactive point of sale.");
        }
        Accreditation accreditation = accreditationMapper.toEntity(request);
        accreditation.setPointOfSaleName(pos.name());
        accreditation.setReceivedAt(LocalDateTime.now());
        Accreditation saved = accreditationRepository.save(accreditation);

        AccreditationCreatedEvent event = accreditationMapper.toAccreditationCreatedEvent(saved);

        System.out.println("Sending event to RabbitMQ: " + event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);

        return accreditationMapper.toAccreditationResponseDTO(saved);
    }

    @Override
    public List<AccreditationResponseDTO> getAllAccreditations() {
        List<AccreditationResponseDTO> cached = accreditationCache.getCachedAccreditations();

        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        List<AccreditationResponseDTO> fromDb = accreditationRepository.findAll().stream()
                .map(accreditationMapper::toAccreditationResponseDTO)
                .collect(Collectors.toList());
        if (!fromDb.isEmpty()) {
            accreditationCache.cacheAccreditations(fromDb);
        }

        return fromDb;
    }
}