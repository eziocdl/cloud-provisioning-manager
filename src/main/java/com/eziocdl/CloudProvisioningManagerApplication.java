package com.eziocdl; // DEVE SER O PACOTE RAIZ

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

// Garante que o Spring varra todos os sub-pacotes, incluindo 'infrastructure'
@SpringBootApplication
@EnableAsync
public class CloudProvisioningManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudProvisioningManagerApplication.class, args);
    }
}