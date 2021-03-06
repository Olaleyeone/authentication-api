package com.olaleyeone.auth.service;

import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.context.TaskContext;
import com.olaleyeone.auth.data.entity.PortalUser;
import com.olaleyeone.auth.data.entity.PortalUserAuthentication;
import com.olaleyeone.auth.data.entity.passwordreset.PasswordResetRequest;
import com.olaleyeone.auth.data.enums.AuthenticationResponseType;
import com.olaleyeone.auth.data.enums.AuthenticationType;
import com.olaleyeone.auth.repository.PortalUserAuthenticationRepository;
import com.olaleyeone.data.dto.RequestMetadata;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import javax.inject.Provider;
import javax.transaction.Transactional;

@Named
@RequiredArgsConstructor
public class ImplicitAuthenticationServiceImpl implements ImplicitAuthenticationService {

    private final Provider<TaskContext> activityLoggerProvider;
    private final PortalUserAuthenticationRepository portalUserAuthenticationRepository;
    private final Provider<RequestMetadata> requestMetadataProvider;

    @Activity("AUTO LOGIN")
    @Transactional
    @Override
    public PortalUserAuthentication createSignUpAuthentication(PortalUser portalUser) {
        RequestMetadata requestMetadata = requestMetadataProvider.get();
        activityLoggerProvider.get().setDescription(
                String.format("Auto login user %s after registration", portalUser.getId()));
        PortalUserAuthentication userAuthentication = new PortalUserAuthentication();
        userAuthentication.setPortalUser(portalUser);
        userAuthentication.setType(AuthenticationType.USER_REGISTRATION);
        userAuthentication.setResponseType(AuthenticationResponseType.SUCCESSFUL);
        userAuthentication.setIpAddress(requestMetadata.getIpAddress());
        userAuthentication.setUserAgent(requestMetadata.getUserAgent());
        return portalUserAuthenticationRepository.save(userAuthentication);
    }

    @Activity("AUTO LOGIN")
    @Transactional
    @Override
    public PortalUserAuthentication createPasswordResetAuthentication(PasswordResetRequest passwordResetRequest) {
        RequestMetadata requestMetadata = requestMetadataProvider.get();
        PortalUser portalUser = passwordResetRequest.getPortalUser();
        activityLoggerProvider.get().setDescription(
                String.format("Auto login user %s after password reset", portalUser.getId()));
        PortalUserAuthentication userAuthentication = new PortalUserAuthentication();
        userAuthentication.setPortalUser(portalUser);
        userAuthentication.setPortalUserIdentifier(passwordResetRequest.getPortalUserIdentifier());

        userAuthentication.setPasswordResetRequest(passwordResetRequest);
        userAuthentication.setType(AuthenticationType.PASSWORD_RESET);
        userAuthentication.setResponseType(AuthenticationResponseType.SUCCESSFUL);
        userAuthentication.setIpAddress(requestMetadata.getIpAddress());
        userAuthentication.setUserAgent(requestMetadata.getUserAgent());
        return portalUserAuthenticationRepository.save(userAuthentication);
    }

}
