package com.jayway.serviceregistry.security;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OAuthUserDetailsService implements AuthenticationUserDetailsService<OpenIDAuthenticationToken> {

    private static final String EMPTY_ATTRIBUTE = "";

    @Override
    public UserDetails loadUserDetails(OpenIDAuthenticationToken token) throws UsernameNotFoundException {
        List<OpenIDAttribute> attributes = token.getAttributes();
        String email = getAttribute(attributes, "email");
        String firstName = getAttribute(attributes, "firstname");
        String lastName = getAttribute(attributes, "lastname");
        return new ServiceRegistryUser(email, firstName, lastName);
    }

    private String getAttribute(List<OpenIDAttribute> attributes, String attributeName) {
        for (OpenIDAttribute attribute : attributes) {
            if (attribute.getName().equals(attributeName)) {
                return attribute.getValues().get(0);
            }
        }
        return EMPTY_ATTRIBUTE;
    }
}