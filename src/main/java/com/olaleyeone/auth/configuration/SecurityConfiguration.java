package com.olaleyeone.auth.configuration;

import com.github.olaleyeone.auth.access.AccessStatus;
import com.github.olaleyeone.auth.access.TrustedIpAddressAuthorizer;
import com.github.olaleyeone.auth.data.AccessClaimsExtractor;
import com.github.olaleyeone.auth.data.AuthorizedRequestFactory;
import com.google.gson.Gson;
import com.olaleyeone.audittrail.impl.TaskContextFactory;
import com.olaleyeone.auth.integration.security.*;
import com.olaleyeone.auth.integration.etc.HashService;
import com.olaleyeone.auth.integration.etc.HashServiceImpl;
import com.olaleyeone.auth.qualifier.JwtToken;
import com.olaleyeone.auth.qualifier.JwtTokenType;
import com.olaleyeone.auth.service.KeyGenerator;
import com.olaleyeone.auth.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Configuration
public class SecurityConfiguration {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @Bean
    public HashService hashService() {
        return new HashServiceImpl();
    }

    @Bean
    public AuthorizedRequestFactory requestMetadataFactory() {
        return beanFactory.createBean(AuthorizedRequestFactory.class);
    }

    @Profile("!test")
    @Bean
    public TrustedIpAddressAuthorizer trustedIpAddressAccessManager(SettingService settingService) {
        return (accessConstraint, ipAddress) -> {
            Optional<String> value = settingService.getString(StringUtils.defaultIfBlank(accessConstraint.value(), "TRUSTED_IP"));
            if (value.isPresent()) {
                return Arrays.asList(value.get().split(" *, *")).contains(ipAddress)
                        ? AccessStatus.allowed()
                        : AccessStatus.denied(ipAddress);
            }
            if (accessConstraint.defaultIpAddresses().length > 0) {
                return Arrays.asList(accessConstraint.defaultIpAddresses()).contains(ipAddress)
                        ? AccessStatus.allowed()
                        : AccessStatus.denied(ipAddress);
            }
            return AccessStatus.denied("");
        };
    }

    @JwtToken(JwtTokenType.ACCESS)
    @Bean
    public TokenGenerator accessTokenGenerator(KeyGenerator keyGenerator, TaskContextFactory taskContextFactory) {
        return AccessTokenGenerator.builder()
                .jwsGenerator(simpleJwsGenerator())
                .keyGenerator(keyGenerator)
                .signingKeyResolver(accessTokenKeyResolver())
                .taskContextFactory(taskContextFactory)
                .build();
    }

    @JwtToken(JwtTokenType.ACCESS)
    @Bean
    public AccessClaimsExtractor accessClaimsExtractor(Gson gson) {
        return new AccessClaimsExtractorImpl(accessTokenKeyResolver(), gson);
    }

    @JwtToken(JwtTokenType.ACCESS)
    @Bean
    public SimpleSigningKeyResolver accessTokenKeyResolver() {
        return beanFactory.createBean(SimpleSigningKeyResolver.class);
    }

    @JwtToken(JwtTokenType.REFRESH)
    @Bean
    public TokenGenerator refreshTokenGenerator(KeyGenerator keyGenerator, TaskContextFactory taskContextFactory) {
        return AccessTokenGenerator.builder()
                .jwsGenerator(simpleJwsGenerator())
                .keyGenerator(keyGenerator)
                .signingKeyResolver(refreshTokenKeyResolver())
                .taskContextFactory(taskContextFactory)
                .build();
    }

    @JwtToken(JwtTokenType.REFRESH)
    @Bean
    public AccessClaimsExtractor refreshTokenClaimsExtractor(Gson gson) {
        return new AccessClaimsExtractorImpl(refreshTokenKeyResolver(), gson);
    }

    @JwtToken(JwtTokenType.REFRESH)
    @Bean
    public SimpleSigningKeyResolver refreshTokenKeyResolver() {
        return beanFactory.createBean(SimpleSigningKeyResolver.class);
    }

    @Bean
    public SimpleJwsGenerator simpleJwsGenerator() {
        return beanFactory.createBean(SimpleJwsGenerator.class);
    }
}