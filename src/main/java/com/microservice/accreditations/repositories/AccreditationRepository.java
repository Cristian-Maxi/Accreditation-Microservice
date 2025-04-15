package com.microservice.accreditations.repositories;

import com.microservice.accreditations.models.Accreditation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccreditationRepository extends JpaRepository<Accreditation, Long> {
}
