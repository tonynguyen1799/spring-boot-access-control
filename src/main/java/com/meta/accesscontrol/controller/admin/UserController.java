package com.meta.accesscontrol.controller.admin;

import com.meta.accesscontrol.controller.admin.payload.*;
import com.meta.accesscontrol.controller.payload.response.JsonResponse;
import com.meta.accesscontrol.controller.payload.response.PaginationResponse;
import com.meta.accesscontrol.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER_MANAGEMENT_READ')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public JsonResponse<PaginationResponse<UserDetailResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String[] sort,
            @Valid UserFilterRequest filterRequest
    ) {
        PaginationResponse<UserDetailResponse> users = userService.getUsers(page, size, sort, filterRequest);
        return new JsonResponse<>(HttpStatus.OK.value(), "Users fetched successfully", users);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_MANAGEMENT_WRITE')")
    public JsonResponse<UserDetailResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDetailResponse newUser = userService.createUser(request);
        return new JsonResponse<>(HttpStatus.CREATED.value(), "User created successfully", newUser);
    }

    @GetMapping("/{textId}")
    public JsonResponse<UserDetailResponse> getUserDetails(@PathVariable String textId) {
        UserDetailResponse user = userService.getUserDetails(textId);
        return new JsonResponse<>(HttpStatus.OK.value(), "User details fetched successfully", user);
    }

    @PutMapping("/{textId}/roles")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT_WRITE')")
    public JsonResponse<UserDetailResponse> updateUserRoles(@PathVariable String textId, @Valid @RequestBody UpdateUserRolesRequest request) {
        UserDetailResponse updatedUser = userService.updateUserRoles(textId, request.roleTextIds());
        return new JsonResponse<>(HttpStatus.OK.value(), "User roles updated successfully", updatedUser);
    }

    @PutMapping("/{textId}/status")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT_WRITE')")
    public JsonResponse<UserDetailResponse> updateUserStatus(@PathVariable String textId, @Valid @RequestBody UpdateUserStatusRequest request) {
        UserDetailResponse updatedUser = userService.updateUserStatus(textId, request.enabled());
        return new JsonResponse<>(HttpStatus.OK.value(), "User status updated successfully", updatedUser);
    }
}
