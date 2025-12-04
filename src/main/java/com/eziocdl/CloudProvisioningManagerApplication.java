package com.eziocdl; // DEVE SER O PACOTE RAIZ

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// Garante que o Spring varra todos os sub-pacotes, incluindo 'infrastructure'
@SpringBootApplication
@ComponentScan(basePackages = "com.eziocdl")
public class CloudProvisioningManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudProvisioningManagerApplication.class, args);
    }
}