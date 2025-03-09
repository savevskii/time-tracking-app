package com.fsavevsk.timetracking.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

    @Value("${keycloak.introspection-uri}")
    private String introspectionUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, OpaqueTokenAuthenticationConverter converter) throws Exception {


        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth->
                        auth.anyRequest().authenticated())
                .oauth2ResourceServer(auth->
                        auth.opaqueToken(op->
                                        op.introspectionUri(this.introspectionUrl)
                                                .introspectionClientCredentials(
                                                        this.clientId,
                                                        this.clientSecret)
                                                .authenticationConverter(converter)
                                )

//                                .authenticationEntryPoint(entryPoint.handler())
//                                .accessDeniedHandler(accessDeniedHandler.handler())
                )
//                .exceptionHandling(ex ->
//                        ex.authenticationEntryPoint(entryPoint.handler())
//                                .accessDeniedHandler(accessDeniedHandler.handler()))
        ;

        return httpSecurity.build();
    }

}