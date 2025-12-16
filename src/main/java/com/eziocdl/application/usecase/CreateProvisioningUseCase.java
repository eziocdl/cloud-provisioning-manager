package com.eziocdl.application.usecase;

import com.eziocdl.application.port.in.CreateProvisioningUseCasePort;
import com.eziocdl.application.port.out.ProvisioningRepositoryPort;
import com.eziocdl.domain.event.ProvisioningRequestedEvent;
import com.eziocdl.domain.model.ProvisioningRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProvisioningUseCase implements CreateProvisioningUseCasePort {

    private final ProvisioningRepositoryPort repository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ProvisioningRequest create(String username, String ram, String cpu) {

        ProvisioningRequest request = new ProvisioningRequest(username, ram, cpu);


        ProvisioningRequest savedRequest = repository.save(request);
        System.out.println("💾 [UseCase] Pedido salvo no DB: " + savedRequest.getId());


        eventPublisher.publishEvent(new ProvisioningRequestedEvent(savedRequest.getId()));

        return savedRequest;
    }
}