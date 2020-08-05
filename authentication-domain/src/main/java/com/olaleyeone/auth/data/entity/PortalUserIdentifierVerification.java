package com.olaleyeone.auth.data.entity;

import com.olaleyeone.audittrail.api.IgnoreData;
import com.olaleyeone.auth.data.enums.UserIdentifierType;
import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@Entity
public class PortalUserIdentifierVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserIdentifierType identifierType;

    @Column(nullable = false)
    private String identifier;

    private String verificationCode;

    @IgnoreData
    @Column(updatable = false, nullable = false)
    private String verificationCodeHash;

    @Column(updatable = false, nullable = false)
    private OffsetDateTime createdOn;
    @Column(updatable = false, nullable = false)
    private OffsetDateTime expiresOn;

    private OffsetDateTime usedOn;
    private OffsetDateTime deactivatedOn;

    @PrePersist
    public void prePersist() {
        if (createdOn != null) {
            return;
        }
        createdOn = OffsetDateTime.now();
    }
}
