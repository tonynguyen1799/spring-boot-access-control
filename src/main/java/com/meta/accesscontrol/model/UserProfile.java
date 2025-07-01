package com.meta.accesscontrol.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private User user;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 255)
    private String address;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 50)
    private String timeZone;

    @Size(max = 10)
    private String locale;

    public UserProfile(User user) {
        this.user = user;
    }
}
