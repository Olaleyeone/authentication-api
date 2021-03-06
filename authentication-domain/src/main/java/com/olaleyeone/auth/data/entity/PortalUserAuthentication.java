package com.olaleyeone.auth.data.entity;

import com.olaleyeone.auth.data.entity.passwordreset.PasswordResetRequest;
import com.olaleyeone.auth.data.enums.AuthenticationResponseType;
import com.olaleyeone.auth.data.enums.AuthenticationType;
import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Optional;

@Entity
@Data
public class PortalUserAuthentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String identifier;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthenticationResponseType responseType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthenticationType type;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String userAgent;

    @ManyToOne
    private PortalUserIdentifier portalUserIdentifier;

    @ManyToOne
    private PortalUser portalUser;

    @OneToOne
    @JoinColumn(unique = true)
    private PasswordResetRequest passwordResetRequest;

    private OffsetDateTime lastActiveAt;
    private OffsetDateTime becomesInactiveAt;

    private OffsetDateTime autoLogoutAt;
    private OffsetDateTime loggedOutAt;
    
    private OffsetDateTime deactivatedAt;

    @Column(updatable = false, nullable = false)
    private OffsetDateTime dateCreated;

    @PrePersist
    public void prePersist() {
        dateCreated = OffsetDateTime.now();
        Optional.ofNullable(portalUserIdentifier)
                .map(PortalUserIdentifier::getPortalUser)
                .ifPresent(portalUser -> {
                    if (this.portalUser == null) {
                        this.portalUser = portalUser;
                    } else if (!this.portalUser.getId().equals(portalUser.getId())) {
                        throw new IllegalArgumentException();
                    }
                });
    }
}
