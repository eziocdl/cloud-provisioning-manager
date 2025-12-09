package com.eziocdl.api.docs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cloud Provisioning Manager (CPM)")
                        .version("1.0.0")
                        .description("""
                            API RESTful para orquestração e governança de infraestrutura multicloud.
                            
                            O sistema atua como um gateway centralizado para provisionamento de recursos computacionais, garantindo conformidade com políticas de segurança e custos.
                            
                            **Capacidades Arquiteturais:**
                            * **Governança de Recursos:** Validação de cotas e políticas de acesso (RBAC) pré-provisionamento.
                            * **Abstração de Nuvem:** Integração agnóstica com provedores de infraestrutura (ex: OpenStack) via Padrão Adapter.
                            * **Observabilidade:** Rastreamento distribuído (Distributed Tracing) nativo em todas as requisições.
                            """)
                        .contact(new Contact()
                                .name("Ezio Lima")
                                .url("https://github.com/eziocdl")
                                .email("dev@eziocdl.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}