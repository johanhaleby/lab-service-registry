package com.jayway.serviceregistry.infrastructure.security;

import com.jayway.serviceregistry.infrastructure.messaging.MessageSender;
import com.jayway.serviceregistry.infrastructure.messaging.Topic;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.LogLevel;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.Messages;
import com.jayway.serviceregistry.infrastructure.messaging.protocol.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;

@Component
public class OAuthUserDetailsService implements AuthenticationUserDetailsService<OpenIDAuthenticationToken> {
    private static final Logger log = LoggerFactory.getLogger(OAuthUserDetailsService.class);

    private static final String EMPTY_ATTRIBUTE = "";

    @Autowired
    MessageSender messageSender;

    @Override
    public UserDetails loadUserDetails(OpenIDAuthenticationToken token) throws UsernameNotFoundException {
        List<OpenIDAttribute> attributes = token.getAttributes();
        String email = getAttribute(attributes, "email");
        String firstName = getAttribute(attributes, "firstname");
        String lastName = getAttribute(attributes, "lastname");


        String message = format("User %s %s (%s) logged in to Service Registry.", firstName, lastName, email);
        log.info(message);
        messageSender.sendMessage(Topic.LOG, Messages.log(LogLevel.INFO, ServiceRegistry.APP_ID, message));
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