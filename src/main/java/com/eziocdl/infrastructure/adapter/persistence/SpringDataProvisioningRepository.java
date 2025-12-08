package com.eziocdl.infrastructure.adapter.persistence;

import com.eziocdl.domain.model.ProvisioningRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataProvisioningRepository extends JpaRepository<ProvisioningRequest, UUID> {

}
