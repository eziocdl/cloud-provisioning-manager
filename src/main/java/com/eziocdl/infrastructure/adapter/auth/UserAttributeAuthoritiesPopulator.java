package com.eziocdl.infrastructure.adapter.auth;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom LdapAuthoritiesPopulator that extracts the user's role
 * from their own LDAP attributes (description field) instead of group membership.
 */
@Component
public class UserAttributeAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    private static final String ROLE_ATTRIBUTE = "description";
    private static final String DEFAULT_ROLE = "TRAINEE";

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(
            DirContextOperations userData, String username) {

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Get role from user's description attribute
        String[] roles = userData.getStringAttributes(ROLE_ATTRIBUTE);

        if (roles != null && roles.length > 0) {
            for (String role : roles) {
                String normalizedRole = role.toUpperCase().trim();
                authorities.add(new SimpleGrantedAuthority(normalizedRole));
                System.out.println("🔐 [LDAP] User '" + username + "' has role: " + normalizedRole);
            }
        } else {
            // Default to TRAINEE (least privilege)
            authorities.add(new SimpleGrantedAuthority(DEFAULT_ROLE));
            System.out.println("🔐 [LDAP] User '" + username + "' has no role, defaulting to: " + DEFAULT_ROLE);
        }

        return authorities;
    }
}
