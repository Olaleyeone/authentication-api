package com.olaleyeone.auth.service;

import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.context.TaskContext;
import com.olaleyeone.auth.data.dto.PasswordResetApiRequest;
import com.olaleyeone.auth.data.dto.PasswordUpdateApiRequest;
import com.olaleyeone.auth.data.entity.PortalUser;
import com.olaleyeone.auth.data.entity.PortalUserAuthentication;
import com.olaleyeone.auth.data.entity.RefreshToken;
import com.olaleyeone.auth.data.entity.passwordreset.PasswordResetRequest;
import com.olaleyeone.auth.integration.security.HashService;
import com.olaleyeone.auth.repository.PasswordResetRequestRepository;
import com.olaleyeone.auth.repository.PortalUserAuthenticationRepository;
import com.olaleyeone.auth.repository.PortalUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;

import javax.inject.Named;
import javax.inject.Provider;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Named
public class PasswordUpdateServiceImpl implements PasswordUpdateService {

    private final Provider<TaskContext> taskContextProvider;
    private final HashService hashService;
    private final PortalUserRepository portalUserRepository;
    private final PortalUserAuthenticationRepository portalUserAuthenticationRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final ImplicitAuthenticationService implicitAuthenticationService;

    @Activity("PASSWORD UPDATE")
    @Transactional
    @Override
    public void updatePassword(RefreshToken refreshToken, PasswordUpdateApiRequest passwordUpdateApiRequest) {
        PortalUser portalUser = refreshToken.getPortalUser();

        taskContextProvider.get().setDescription(
                String.format("Updating password for logged in user %s", portalUser.getId()));

        portalUser.setPassword(hashService.generateHash(passwordUpdateApiRequest.getPassword()));
        portalUser.setPasswordLastUpdatedOn(OffsetDateTime.now());
        portalUser.setPasswordUpdateRequired(false);
        portalUserRepository.save(portalUser);

        if (BooleanUtils.isTrue(passwordUpdateApiRequest.getInvalidateOtherSessions())) {
            portalUserAuthenticationRepository.deactivateOtherSessions(refreshToken.getActualAuthentication());
        }
    }

    @Activity("PASSWORD RESET")
    @Transactional
    @Override
    public PortalUserAuthentication updatePassword(PasswordResetRequest passwordResetRequest, PasswordResetApiRequest passwordUpdateApiRequest) {
        PortalUser portalUser = passwordResetRequest.getPortalUser();
        taskContextProvider.get().setDescription(
                String.format("Password reset by user %s", portalUser.getId()));

        portalUser.setPassword(hashService.generateHash(passwordUpdateApiRequest.getPassword()));
        portalUser.setPasswordLastUpdatedOn(OffsetDateTime.now());
        portalUser.setPasswordUpdateRequired(false);
        portalUserRepository.save(portalUser);

        passwordResetRequest.setUsedOn(OffsetDateTime.now());
        passwordResetRequestRepository.save(passwordResetRequest);

        if (BooleanUtils.isTrue(passwordUpdateApiRequest.getInvalidateOtherSessions())) {
            portalUserAuthenticationRepository.deactivateOtherSessions(portalUser);
        }
        return implicitAuthenticationService.createPasswordResetAuthentication(passwordResetRequest);
    }
}
