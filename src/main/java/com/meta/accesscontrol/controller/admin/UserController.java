package com.meta.accesscontrol.controller.admin;

import com.meta.accesscontrol.controller.admin.payload.*;
import com.meta.accesscontrol.controller.payload.response.JsonResponse;
import com.meta.accesscontrol.controller.payload.response.PaginationResponse;
import com.meta.accesscontrol.controller.payload.response.UserResponse;
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
    public JsonResponse<PaginationResponse<UserResponse>> getUsers(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(required = false) String[] sort,
                                                                   @Valid UserFilterRequest filterRequest
    ) {
        PaginationResponse<UserResponse> users = userService.getUsers(page, size, sort, filterRequest);
        return new JsonResponse<>(HttpStatus.OK.value(), "Users fetched successfully", users);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_MANAGEMENT_WRITE')")
    public JsonResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse newUser = userService.createUser(request);
        return new JsonResponse<>(HttpStatus.CREATED.value(), "User created successfully", newUser);
    }

    @GetMapping("/{textId}")
    public JsonResponse<UserResponse> getUser(@PathVariable String textId) {
        UserResponse user = userService.getUser(textId);
        return new JsonResponse<>(HttpStatus.OK.value(), "User details fetched successfully", user);
    }

    @PutMapping("/{textId}")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT_WRITE')")
    public JsonResponse<UserResponse> updateUser(@PathVariable String textId, @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updatedUser = userService.updateUser(textId, request);
        return new JsonResponse<>(HttpStatus.OK.value(), "User updated successfully", updatedUser);
    }

    @DeleteMapping("/{textId}")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT_WRITE')")
    public JsonResponse<Void> deleteUser(@PathVariable String textId) {
        userService.deleteUser(textId);
        return new JsonResponse<>(HttpStatus.OK.value(), "User deleted successfully", null);
    }
}
