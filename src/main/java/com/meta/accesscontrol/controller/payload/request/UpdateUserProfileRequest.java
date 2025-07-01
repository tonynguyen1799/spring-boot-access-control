package com.meta.accesscontrol.controller.payload.request;

import com.meta.accesscontrol.model.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserProfileRequest(
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName,
        @Past LocalDate dateOfBirth,
        Gender gender,
        @Size(max = 20) String phoneNumber,
        @Size(max = 255) String address,
        @Size(max = 255) String avatarUrl,
        @Size(max = 50) String timeZone,
        @Size(max = 10) String locale
) {}
