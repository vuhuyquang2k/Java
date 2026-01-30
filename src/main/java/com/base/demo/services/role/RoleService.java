package com.base.demo.services.role;

import com.base.demo.dtos.role.CreateRoleRequest;
import com.base.demo.dtos.role.GetRoleResponse;
import com.base.demo.dtos.role.UpdateRoleRequest;

import java.util.List;

public interface RoleService {
    List<GetRoleResponse> getRoles(String role, String description, int page, int size);
    void createRole(CreateRoleRequest request);
    void updateRole(Integer id, UpdateRoleRequest request);
    void deleteRole(Integer id);
}
