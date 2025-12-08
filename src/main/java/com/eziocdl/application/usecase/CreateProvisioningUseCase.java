package com.eziocdl.application.usecase;

import com.eziocdl.application.port.in.CreateProvisioningUseCasePort;
import com.eziocdl.application.port.out.ProvisioningRepositoryPort;
import com.eziocdl.domain.model.ProvisioningRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProvisioningUseCase implements CreateProvisioningUseCasePort {


    private final ProvisioningRepositoryPort repository;


    public CreateProvisioningUseCase(ProvisioningRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ProvisioningRequest create(String username, String ram, String cpu) {

        var request = new ProvisioningRequest(username, ram, cpu);

        return repository.save(request);
    }
}