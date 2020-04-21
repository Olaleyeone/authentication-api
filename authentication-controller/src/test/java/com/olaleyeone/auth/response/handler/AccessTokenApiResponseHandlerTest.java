package com.olaleyeone.auth.response.handler;

import com.olaleyeone.auth.data.entity.PortalUser;
import com.olaleyeone.auth.data.entity.PortalUserAuthentication;
import com.olaleyeone.auth.data.entity.PortalUserIdentifier;
import com.olaleyeone.auth.data.entity.RefreshToken;
import com.olaleyeone.auth.data.enums.AuthenticationResponseType;
import com.olaleyeone.auth.dto.JwtDto;
import com.olaleyeone.auth.response.pojo.AccessTokenApiResponse;
import com.olaleyeone.auth.service.JwtService;
import com.olaleyeone.auth.service.RefreshTokenService;
import com.olaleyeone.auth.test.ComponentTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.net.HttpCookie;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AccessTokenApiResponseHandlerTest extends ComponentTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AccessTokenApiResponseHandler handler;

    private PortalUser user;
    private PortalUserIdentifier userIdentifier;
    private RefreshToken refreshToken;
    private PortalUserAuthentication userAuthentication;

    private JwtDto refreshJwt;
    private JwtDto accessJwt;

    @BeforeEach
    void setUp() {

        user = new PortalUser();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());

        userIdentifier = new PortalUserIdentifier();
        userIdentifier.setPortalUser(user);

        userAuthentication = new PortalUserAuthentication();
        userAuthentication.setResponseType(AuthenticationResponseType.SUCCESSFUL);
        userAuthentication.setPortalUserIdentifier(userIdentifier);
        userAuthentication.setPortalUser(userIdentifier.getPortalUser());

        refreshToken = new RefreshToken();
        refreshToken.setActualAuthentication(userAuthentication);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        refreshJwt = JwtDto.builder()
                .token(UUID.randomUUID().toString())
                .secondsTillExpiry(faker.number().randomNumber())
                .build();
        accessJwt = refreshJwt;


        Mockito.when(refreshTokenService.createRefreshToken(Mockito.any(PortalUserAuthentication.class)))
                .then(invocation -> refreshToken);
        Mockito.when(jwtService.generateJwt(Mockito.any()))
                .then(invocation -> refreshJwt);
        Mockito.when(jwtService.generateJwt(Mockito.any()))
                .then(invocation -> accessJwt);
    }

    @Test
    public void getUserPojoForAuthentication() {
        HttpEntity<AccessTokenApiResponse> responseEntity = handler.getAccessToken(userAuthentication);
        AccessTokenApiResponse accessTokenApiResponse = responseEntity.getBody();
        assertEquals(user.getFirstName(), accessTokenApiResponse.getFirstName());
        assertEquals(user.getLastName(), accessTokenApiResponse.getLastName());
        assertNull(accessTokenApiResponse.getRefreshToken());
        assertNull(accessTokenApiResponse.getAccessToken());
    }

    @Test
    public void shouldReturnCacheHeaders() {
        HttpEntity<AccessTokenApiResponse> responseEntity = handler.getAccessToken(userAuthentication);
        assertEquals("no-store", responseEntity.getHeaders().getCacheControl());
        assertEquals("no-cache", responseEntity.getHeaders().getPragma());
    }

    @Test
    public void shouldReturnAccessTokenCookies() {

        HttpEntity<AccessTokenApiResponse> responseEntity = handler.getAccessToken(userAuthentication);

        List<HttpCookie> httpCookies = getCookiesByName(responseEntity, "access_token");
        assertEquals(1, httpCookies.size());
        HttpCookie httpCookie = httpCookies.iterator().next();
        assertEquals(accessJwt.getSecondsTillExpiry(), httpCookie.getMaxAge());
        assertSecure(httpCookie);
    }

    @Test
    public void shouldReturnRefreshTokenCookies() {
        HttpEntity<AccessTokenApiResponse> responseEntity = handler.getAccessToken(userAuthentication);

        List<HttpCookie> httpCookies = getCookiesByName(responseEntity, "refresh_token");
        assertEquals(1, httpCookies.size());
        HttpCookie httpCookie = httpCookies.iterator().next();
        assertTrue((refreshToken.getSecondsTillExpiry() - httpCookie.getMaxAge()) <= 1);
        assertSecure(httpCookie);
    }

    private List<HttpCookie> getCookiesByName(HttpEntity<?> responseEntity, String name) {
        return HttpCookie.parse(collectCookies(responseEntity))
                .stream().filter(httpCookie -> httpCookie.getName().equals(name))
                .collect(Collectors.toList());
    }

    private String collectCookies(HttpEntity<?> responseEntity) {
        return responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE)
                .stream().collect(Collectors.joining(", "));
    }

    private void assertSecure(HttpCookie httpCookie) {
        assertTrue(httpCookie.getSecure());
        assertTrue(httpCookie.isHttpOnly());
    }
}