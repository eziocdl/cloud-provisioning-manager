package com.eziocdl.application.usecase;

import com.eziocdl.application.port.in.CreateProvisioningUseCasePort;
import com.eziocdl.application.port.out.ProvisioningRepositoryPort;
import com.eziocdl.domain.event.ProvisioningRequestedEvent;
import com.eziocdl.domain.model.ProvisioningRequest;
import com.eziocdl.domain.service.PolicyEnforcementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProvisioningUseCase implements CreateProvisioningUseCasePort {

    private final ProvisioningRepositoryPort repository;
    private final ApplicationEventPublisher eventPublisher;
    private final PolicyEnforcementService policyEnforcementService;

    @Override
    @Transactional
    public ProvisioningRequest create(String username, String ram, String cpu) {

        // 1. Extract user role from security context
        String userRole = extractUserRole();

        // 2. Enforce governance policies BEFORE processing
        policyEnforcementService.enforce(userRole, ram, cpu);

        // 3. Create and persist the request
        ProvisioningRequest request = new ProvisioningRequest(username, ram, cpu);
        ProvisioningRequest savedRequest = repository.save(request);
        System.out.println("💾 [UseCase] Pedido salvo no DB: " + savedRequest.getId());

        // 4. Publish event for async processing
        eventPublisher.publishEvent(new ProvisioningRequestedEvent(savedRequest.getId()));

        return savedRequest;
    }

    private String extractUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return "TRAINEE"; // Least privilege
        }

        // Get the first authority/role
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .findFirst()
                .orElse("TRAINEE");
    }
}