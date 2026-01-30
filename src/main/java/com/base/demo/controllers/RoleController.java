package com.base.demo.controllers;

import com.base.demo.dtos.common.ApiResponse;
import com.base.demo.dtos.role.CreateRoleRequest;
import com.base.demo.dtos.role.GetRoleResponse;
import com.base.demo.dtos.role.UpdateRoleRequest;
import com.base.demo.entities.Role;
import com.base.demo.services.role.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<GetRoleResponse>>> getRoles(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getRoles(name, description, page, size)));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> createRole(@RequestBody @Valid CreateRoleRequest request) {
        roleService.createRole(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{id}")
    public  ResponseEntity<ApiResponse<Void>> updateRole(@PathVariable Integer id, @RequestBody @Valid UpdateRoleRequest request) {
        roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
