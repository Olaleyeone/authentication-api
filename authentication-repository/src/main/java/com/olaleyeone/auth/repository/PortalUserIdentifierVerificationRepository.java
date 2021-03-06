package com.olaleyeone.auth.repository;

import com.olaleyeone.auth.data.entity.PortalUserIdentifierVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PortalUserIdentifierVerificationRepository extends JpaRepository<PortalUserIdentifierVerification, Long> {

    @Query("SELECT v FROM PortalUserIdentifierVerification v" +
            " WHERE lower(v.identifier)=lower(?1)" +
            " AND v.usedOn IS NULL" +
            " AND v.deactivatedOn IS NULL" +
            " AND v.expiresOn>=CURRENT_TIMESTAMP")
    List<PortalUserIdentifierVerification> getAllActive(String identifier);
}
