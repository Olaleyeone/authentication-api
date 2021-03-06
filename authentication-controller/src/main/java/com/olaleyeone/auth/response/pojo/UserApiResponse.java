package com.olaleyeone.auth.response.pojo;

import com.olaleyeone.auth.data.entity.PortalUser;
import com.olaleyeone.auth.data.enums.Gender;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class UserApiResponse {

    private Long id;
    private String displayName;
    private String firstName;
    private String lastName;

    private Gender gender;

    private Boolean passwordUpdateRequired;
    private OffsetDateTime passwordLastUpdatedOn;

    private OffsetDateTime lastActiveOn;

    private List<UserIdentifierApiResponse> identifiers;
    private Set<String> emailAddresses;
    private Set<String> phoneNumbers;

    private List<UserDataApiResponse> data;

    public UserApiResponse(PortalUser portalUser) {
        this.id = portalUser.getId();
        this.displayName = portalUser.getDisplayName();
        this.firstName = portalUser.getFirstName();
        this.lastName = portalUser.getLastName();
        this.gender = portalUser.getGender();
        this.passwordUpdateRequired = portalUser.getPasswordUpdateRequired();
        this.passwordLastUpdatedOn = portalUser.getPasswordLastUpdatedOn();
    }
}
