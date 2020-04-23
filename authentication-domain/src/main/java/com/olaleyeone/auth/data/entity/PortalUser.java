package com.olaleyeone.auth.data.entity;

import com.olaleyeone.audittrail.api.IgnoreData;
import com.olaleyeone.auth.data.embeddable.PersistTimeSetter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import javax.persistence.*;

@Entity
@Data
public class PortalUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @IgnoreData
    private String password;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    private String otherName;

    @Embedded
    @Delegate
    @Setter(value = AccessLevel.NONE)
    @Getter(value = AccessLevel.NONE)
    private PersistTimeSetter persistTimeSetter = new PersistTimeSetter();
}
