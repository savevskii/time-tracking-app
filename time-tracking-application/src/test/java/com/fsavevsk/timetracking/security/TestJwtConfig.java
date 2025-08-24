package com.fsavevsk.timetracking.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@TestConfiguration(proxyBeanMethods = false)
public class TestJwtConfig {

    @Bean
    @Primary
    public KeyPair testRsaKeyPair() throws Exception {
        var kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder(KeyPair kp) {
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        return NimbusJwtDecoder.withPublicKey(pub).build();
    }

    @Bean
    @Primary
    public JwtEncoder testJwtEncoder(KeyPair kp) {
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();
        var jwk = new RSAKey.Builder(pub).privateKey(priv).keyID("test").build();
        var jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }
}
