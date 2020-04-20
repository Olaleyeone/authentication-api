package com.olaleyeone.auth.service;

import com.olaleyeone.auth.data.entity.PortalUser;
import com.olaleyeone.auth.data.entity.RefreshToken;
import com.olaleyeone.auth.test.ComponentTest;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest extends ComponentTest {

    private JwtServiceImpl jwtService;

    @Mock
    private SettingService settingService;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtServiceImpl(Keys.secretKeyFor(SignatureAlgorithm.HS256), settingService);
    }

    @Test
    void getRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setExpiresAt(LocalDateTime.now().plusMinutes(1));
        String jws = jwtService.getRefreshToken(refreshToken);
        assertNotNull(jws);
        assertEquals(refreshToken.getId().toString(), jwtService.getSubject(jws));
    }

    @Test
    void shouldFailForExpiredRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setExpiresAt(LocalDateTime.now());
        String jws = jwtService.getRefreshToken(refreshToken);
        assertNotNull(jws);
        assertThrows(ExpiredJwtException.class, () -> jwtService.getSubject(jws));
    }

    @Test
    void getAccessToken() {
        int accessTokenDurationInSeconds = 20;
        Mockito.when(settingService.getInteger(Mockito.any(), Mockito.anyInt()))
                .thenReturn(accessTokenDurationInSeconds);
        PortalUser portalUser = new PortalUser();
        portalUser.setId(1L);
        String jws = jwtService.getAccessToken(portalUser).getToken();
        assertNotNull(jws);
        assertEquals(portalUser.getId().toString(), jwtService.getSubject(jws));
    }

    @Test
    void shouldFailForExpiredAccessToken() {
        int accessTokenDurationInSeconds = -20;
        Mockito.when(settingService.getInteger(Mockito.any(), Mockito.anyInt()))
                .thenReturn(accessTokenDurationInSeconds);
        PortalUser portalUser = new PortalUser();
        portalUser.setId(1L);
        String jws = jwtService.getAccessToken(portalUser).getToken();
        assertNotNull(jws);
        assertThrows(ExpiredJwtException.class, () -> jwtService.getSubject(jws));
    }
}