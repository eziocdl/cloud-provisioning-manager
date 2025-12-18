# ADR-0002: Autenticacao via LDAP Corporativo

## Status

**Accepted**

## Context

O CPM precisa autenticar usuarios e extrair seus cargos (roles) para aplicar politicas de governanca. As opcoes consideradas foram:

1. **Basic Auth com usuarios em banco**: Simples, mas nao integra com ambiente corporativo
2. **OAuth2/OIDC**: Padrao moderno, mas requer Identity Provider externo
3. **JWT auto-assinado**: Sem estado, mas precisa de sistema de login separado
4. **LDAP**: Integra com Active Directory/OpenLDAP corporativo

### Requisitos

- Autenticar contra diretorio corporativo existente
- Extrair roles (TRAINEE, DEV, ADMIN) para aplicar quotas
- Nao armazenar senhas na aplicacao
- Compativel com ferramentas como curl/Postman para testes

## Decision

Adotar **HTTP Basic Authentication sobre LDAP** com Spring Security.

### Configuracao

```java
@Bean
public LdapAuthenticationProvider ldapAuthenticationProvider(...) {
    BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
    bindAuthenticator.setUserDnPatterns(new String[]{"uid={0},ou=users"});
    return new LdapAuthenticationProvider(bindAuthenticator, authoritiesPopulator);
}
```

### Extracao de Roles

Roles sao extraidos do atributo `description` do usuario LDAP via `UserAttributeAuthoritiesPopulator`.

### Usuarios de Teste

Arquivo `infra/ldap/ldap-data.ldif` contem usuarios para desenvolvimento:
- `admin / senhaadmin123` -> ADMIN
- `devuser / senhadev123` -> DEV
- `trainee / senhatrainee123` -> TRAINEE

## Consequences

### Positivas

- **Integracao corporativa**: Funciona com Active Directory/OpenLDAP existente
- **Sem armazenamento de senhas**: Credenciais validadas diretamente no LDAP
- **Simplicidade**: Basic Auth e facil de testar com curl
- **Roles dinamicos**: Mudancas no LDAP refletem imediatamente

### Negativas

- **Stateless**: Cada request requer bind no LDAP (mitigado com connection pooling)
- **Basic Auth**: Menos seguro que tokens (requer HTTPS em producao)
- **Dependencia**: Se LDAP cair, ninguem autentica (nao ha fallback)

### Melhorias Futuras

1. Adicionar cache de autenticacao com TTL curto
2. Implementar fallback para banco local em caso de falha do LDAP
3. Migrar para OAuth2/OIDC com Keycloak para cenarios mais complexos

## References

- [Spring Security LDAP](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/ldap.html)
- [OpenLDAP Documentation](https://www.openldap.org/doc/)
