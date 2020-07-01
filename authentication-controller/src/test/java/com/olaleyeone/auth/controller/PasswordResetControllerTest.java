package com.olaleyeone.auth.controller;

import com.github.olaleyeone.auth.data.AccessClaims;
import com.github.olaleyeone.auth.data.AccessClaimsExtractor;
import com.olaleyeone.auth.controllertest.ControllerTest;
import com.olaleyeone.auth.data.dto.PasswordUpdateApiRequest;
import com.olaleyeone.auth.data.entity.PortalUserAuthentication;
import com.olaleyeone.auth.data.entity.PortalUserIdentifier;
import com.olaleyeone.auth.data.entity.passwordreset.PasswordResetRequest;
import com.olaleyeone.auth.data.enums.JwtTokenType;
import com.olaleyeone.auth.qualifier.JwtToken;
import com.olaleyeone.auth.repository.PasswordResetRequestRepository;
import com.olaleyeone.auth.response.handler.AccessTokenApiResponseHandler;
import com.olaleyeone.auth.service.PasswordUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasswordResetControllerTest extends ControllerTest {

    @Autowired
    private PasswordResetRequestRepository passwordResetRequestRepository;
    @Autowired
    private PasswordUpdateService passwordUpdateService;
    @Autowired
    private AccessTokenApiResponseHandler accessTokenApiResponseHandler;
    @Autowired
    @JwtToken(JwtTokenType.PASSWORD_RESET)
    private AccessClaimsExtractor accessClaimsExtractor;

    private PasswordResetRequest passwordResetRequest;
    private PortalUserIdentifier portalUserIdentifier;

    private PasswordUpdateApiRequest apiRequest;

    private String emailAddress;
    private String resetToken;

    @BeforeEach
    public void setUp() {
        portalUserIdentifier = new PortalUserIdentifier();
        passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setPortalUserIdentifier(portalUserIdentifier);
        passwordResetRequest.setExpiresOn(LocalDateTime.now().plusMinutes(20));

        emailAddress = faker.internet().emailAddress();
        resetToken = faker.lorem().sentence();

        apiRequest = new PasswordUpdateApiRequest();
        apiRequest.setPassword(faker.internet().password());
    }

    @Test
    void resetPasswordWithInvalidResetToken() throws Exception {
        Mockito.doThrow(RuntimeException.class).when(accessClaimsExtractor).getClaims(Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.put("/password")
                .param("identifier", emailAddress)
                .param("resetToken", resetToken)
                .with(body(apiRequest)))
                .andExpect(status().isForbidden());
        Mockito.verify(accessClaimsExtractor, Mockito.times(1))
                .getClaims(resetToken);
    }

    @Test
    void resetPasswordWithUnknownRequest() throws Exception {
        AccessClaims accessClaims = initAccessClaims();

        Mockito.doReturn(Optional.empty()).when(passwordResetRequestRepository).findById(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.put("/password")
                .param("identifier", emailAddress)
                .param("resetToken", resetToken)
                .with(body(apiRequest)))
                .andExpect(status().isForbidden());

        Mockito.verify(accessClaimsExtractor, Mockito.times(1))
                .getClaims(resetToken);
        Mockito.verify(passwordResetRequestRepository, Mockito.times(1))
                .findById(Long.valueOf(accessClaims.getId()));
    }

    @Test
    void resetPasswordWithExpiredRequest() throws Exception {
        AccessClaims accessClaims = initAccessClaims();

        Mockito.doReturn(Optional.of(passwordResetRequest)).when(passwordResetRequestRepository).findById(Mockito.any());
        passwordResetRequest.setExpiresOn(LocalDateTime.now());

        mockMvc.perform(MockMvcRequestBuilders.put("/password")
                .param("identifier", emailAddress)
                .param("resetToken", resetToken)
                .with(body(apiRequest)))
                .andExpect(status().isForbidden());
        Mockito.verify(accessClaimsExtractor, Mockito.times(1))
                .getClaims(resetToken);
        Mockito.verify(passwordResetRequestRepository, Mockito.times(1))
                .findById(Long.valueOf(accessClaims.getId()));
    }

    @Test
    void resetPasswordWithUsedRequest() throws Exception {
        AccessClaims accessClaims = initAccessClaims();

        Mockito.doReturn(Optional.of(passwordResetRequest)).when(passwordResetRequestRepository).findById(Mockito.any());
        passwordResetRequest.setUsedOn(LocalDateTime.now());

        mockMvc.perform(MockMvcRequestBuilders.put("/password")
                .param("identifier", emailAddress)
                .param("resetToken", resetToken)
                .with(body(apiRequest)))
                .andExpect(status().isForbidden());

        Mockito.verify(accessClaimsExtractor, Mockito.times(1))
                .getClaims(resetToken);
        Mockito.verify(passwordResetRequestRepository, Mockito.times(1))
                .findById(Long.valueOf(accessClaims.getId()));
    }

    @Test
    void resetPasswordWithResetToken() throws Exception {

        PortalUserAuthentication userAuthentication = new PortalUserAuthentication();

        AccessClaims accessClaims = initAccessClaims();

        Mockito.doReturn(Optional.of(passwordResetRequest)).when(passwordResetRequestRepository).findById(Mockito.any());
        Mockito.doReturn(userAuthentication).when(passwordUpdateService).updatePassword(
                Mockito.any(PasswordResetRequest.class),
                Mockito.any(PasswordUpdateApiRequest.class));

        portalUserIdentifier.setIdentifier(emailAddress);

        mockMvc.perform(MockMvcRequestBuilders.put("/password")
                .param("identifier", emailAddress)
                .param("resetToken", resetToken)
                .with(body(apiRequest)))
                .andExpect(status().isOk());

        Mockito.verify(accessClaimsExtractor, Mockito.times(1))
                .getClaims(resetToken);
        Mockito.verify(passwordResetRequestRepository, Mockito.times(1))
                .findById(Long.valueOf(accessClaims.getId()));

        Mockito.verify(passwordUpdateService, Mockito.times(1)).updatePassword(passwordResetRequest, apiRequest);
        Mockito.verify(accessTokenApiResponseHandler, Mockito.times(1)).getAccessToken(userAuthentication);
    }

    @Test
    void resetPasswordWithIdentifierMismatch() throws Exception {

        AccessClaims accessClaims = initAccessClaims();

        Mockito.doReturn(Optional.of(passwordResetRequest)).when(passwordResetRequestRepository).findById(Mockito.any());

        portalUserIdentifier.setIdentifier(UUID.randomUUID().toString());

        mockMvc.perform(MockMvcRequestBuilders.put("/password")
                .param("identifier", emailAddress)
                .param("resetToken", resetToken)
                .with(body(apiRequest)))
                .andExpect(status().isForbidden());
        Mockito.verify(accessClaimsExtractor, Mockito.times(1))
                .getClaims(resetToken);
        Mockito.verify(passwordResetRequestRepository, Mockito.times(1))
                .findById(Long.valueOf(accessClaims.getId()));
    }

    private AccessClaims initAccessClaims() {
        AccessClaims accessClaims = Mockito.mock(AccessClaims.class);
        Mockito.doReturn(faker.number().digit()).when(accessClaims).getId();
        Mockito.doReturn(accessClaims).when(accessClaimsExtractor).getClaims(Mockito.any());
        return accessClaims;
    }
}