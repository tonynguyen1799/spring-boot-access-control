package com.meta.accesscontrol.controller.payload.response;

import com.meta.accesscontrol.model.Gender;
import com.meta.accesscontrol.model.UserProfile;

import java.time.LocalDate;

public record UserProfileResponse(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        String phoneNumber,
        String address,
        String avatarUrl,
        String timeZone,
        String locale
) {
    public static UserProfileResponse fromUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }
        return new UserProfileResponse(
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getDateOfBirth(),
                userProfile.getGender(),
                userProfile.getPhoneNumber(),
                userProfile.getAddress(),
                userProfile.getAvatarUrl(),
                userProfile.getTimeZone(),
                userProfile.getLocale()
        );
    }
}
