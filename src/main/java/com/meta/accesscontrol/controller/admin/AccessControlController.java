package com.meta.accesscontrol.controller.admin;

import com.meta.accesscontrol.controller.admin.payload.CreateRoleRequest;
import com.meta.accesscontrol.controller.admin.payload.PrivilegeResponse;
import com.meta.accesscontrol.controller.admin.payload.RoleResponse;
import com.meta.accesscontrol.controller.admin.payload.UpdateRoleRequest;
import com.meta.accesscontrol.controller.payload.response.JsonResponse;
import com.meta.accesscontrol.model.Privilege;
import com.meta.accesscontrol.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin/access-control")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_MANAGEMENT_READ')")
public class AccessControlController {

    private final RoleService roleService;

    @GetMapping("/roles")
    public JsonResponse<List<RoleResponse>> getRoles() {
        List<RoleResponse> roles = roleService.getRoles();
        return new JsonResponse<>(HttpStatus.OK.value(), "Roles fetched successfully", roles);
    }

    @GetMapping("/roles/{textId}")
    public JsonResponse<RoleResponse> getRole(@PathVariable String textId) {
        RoleResponse role = roleService.getRole(textId);
        return new JsonResponse<>(HttpStatus.OK.value(), "Role fetched successfully", role);
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_MANAGEMENT_WRITE')")
    public JsonResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse newRole = roleService.createRole(request);
        return new JsonResponse<>(HttpStatus.CREATED.value(), "Role created successfully", newRole);
    }

    @PutMapping("/roles/{textId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGEMENT_WRITE')")
    public JsonResponse<RoleResponse> updateRole(@PathVariable String textId, @Valid @RequestBody UpdateRoleRequest request) {
        RoleResponse updatedRole = roleService.updateRole(textId, request);
        return new JsonResponse<>(HttpStatus.OK.value(), "Role updated successfully", updatedRole);
    }

    @DeleteMapping("/roles/{textId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGEMENT_WRITE')")
    public JsonResponse<Void> deleteRole(@PathVariable String textId) {
        roleService.deleteRole(textId);
        return new JsonResponse<>(HttpStatus.OK.value(), "Role deleted successfully", null);
    }

    @GetMapping("/privileges")
    public JsonResponse<List<PrivilegeResponse>> getPrivileges() {
        List<PrivilegeResponse> privileges = Arrays.stream(Privilege.values())
                .map(PrivilegeResponse::fromPrivilege)
                .toList();
        return new JsonResponse<>(HttpStatus.OK.value(), "Privileges fetched successfully", privileges);
    }
}