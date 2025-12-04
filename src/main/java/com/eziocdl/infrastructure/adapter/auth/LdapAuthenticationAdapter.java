package com.eziocdl.infrastructure.adapter.auth;

import com.eziocdl.application.port.out.AuthenticationPort; // Import do Port
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

@Component
public class LdapAuthenticationAdapter implements AuthenticationPort {

    private final LdapAuthenticationProvider ldapAuthenticationProvider;

    // A injeção que está falhando (será resolvida pelo @ComponentScan)
    public LdapAuthenticationAdapter(LdapAuthenticationProvider ldapAuthenticationProvider) {
        this.ldapAuthenticationProvider = ldapAuthenticationProvider;
    }

    @Override
    public boolean isAuthenticated(String username, String password) {
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        try {
            Authentication authentication = ldapAuthenticationProvider.authenticate(authenticationToken);
            return authentication != null && authentication.isAuthenticated();
        } catch (BadCredentialsException e) {
            return false;
        } catch (Exception e) {
            System.err.println("LDAP Authentication Error for user " + username + ": " + e.getMessage());
            return false;
        }
    }
}