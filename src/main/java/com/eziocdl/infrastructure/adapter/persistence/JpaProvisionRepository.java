package com.eziocdl.infrastructure.adapter.persistence;

import com.eziocdl.application.port.out.ProvisioningRepositoryPort;
import com.eziocdl.domain.model.ProvisioningRequest;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public class JpaProvisionRepository implements ProvisioningRepositoryPort {


    private final SpringDataProvisioningRepository repository;

    public JpaProvisionRepository(SpringDataProvisioningRepository repository){
        this.repository = repository;
    }
    @Override
    public ProvisioningRequest save(ProvisioningRequest request) {
        return repository.save(request);
    }

    @Override
    public Optional<ProvisioningRequest> findById(UUID id) {
        return repository.findById(id);
    }
}
