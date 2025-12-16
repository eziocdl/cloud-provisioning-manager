package com.eziocdl.application.listener;

import com.eziocdl.application.port.out.CloudProviderPort;
import com.eziocdl.application.port.out.ProvisioningRepositoryPort;
import com.eziocdl.domain.event.ProvisioningRequestedEvent;
import com.eziocdl.domain.model.ProvisioningRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProvisioningAsyncListener {

    private final CloudProviderPort cloudProviderPort;
    private final ProvisioningRepositoryPort repository;

    @Async

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProvisioningRequested(ProvisioningRequestedEvent event) {
        System.out.println("⚡ [Listener] Thread separada iniciada (Pós-Commit) para ID: " + event.provisioningId());

        ProvisioningRequest request = repository.findById(event.provisioningId())
                .orElseThrow(() -> new RuntimeException("Pedido sumiu do banco!"));

        try {

            String instanceId = cloudProviderPort.provisionInstance(request);
            System.out.println("[Listener] Sucesso! ID Nuvem: " + instanceId);


            request.complete();
            repository.save(request);

        } catch (Exception e) {
            System.err.println(" [Listener] Falha: " + e.getMessage());
            request.fail();
            repository.save(request);
        }
    }
}