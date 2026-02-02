package com.base.demo.services.role;

import com.base.demo.dtos.role.CreateRoleRequest;
import com.base.demo.dtos.role.GetRoleResponse;
import com.base.demo.dtos.role.UpdateRoleRequest;
import com.base.demo.entities.Role;
import com.base.demo.exceptions.ConflictException;
import com.base.demo.exceptions.ResourceNotFoundException;
import com.base.demo.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<GetRoleResponse> getRoles(String role, String description, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<Role> roles = roleRepository.findByFilters(role, description, pageable);
        return roles.stream().map(r -> GetRoleResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build()).toList();
    }

    @Override
    public void createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new ConflictException("Role với tên '" + request.getName() + "' đã tồn tại");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        roleRepository.save(role);
    }

    @Override
    public void updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (request.getName() != null && !request.getName().equals(role.getName())) {
            if (roleRepository.existsByName(request.getName())) {
                throw new ConflictException("Role với tên '" + request.getName() + "' đã tồn tại");
            }

            role.setName(request.getName());
        }

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        roleRepository.save(role);
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role", "id", id);
        }

        roleRepository.deleteById(id);
    }

}
