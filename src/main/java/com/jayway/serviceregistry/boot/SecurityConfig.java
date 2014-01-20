package com.jayway.serviceregistry.boot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAuthenticationToken;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/resources/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .openidLogin() // Defined in OpenIDLoginConfigurer
                .loginPage("/login.html")
                .permitAll()
                .authenticationUserDetailsService(new CustomUserDetailsService());
    }

    @Bean(name = "myAuthenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    class CustomUserDetailsService implements AuthenticationUserDetailsService<OpenIDAuthenticationToken> {

        @Override
        public UserDetails loadUserDetails(OpenIDAuthenticationToken token) throws UsernameNotFoundException {
            return new User(token.getName(), "", AuthorityUtils.createAuthorityList("ROLE_USER"));
        }
    }
}