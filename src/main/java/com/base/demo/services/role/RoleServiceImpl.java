package com.base.demo.services.role;

import com.base.demo.dtos.role.CreateRoleRequest;
import com.base.demo.dtos.role.GetRoleResponse;
import com.base.demo.dtos.role.UpdateRoleRequest;
import com.base.demo.entities.Role;
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
                .build()
        ).toList();
    }

    @Override
    public void createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Role with name " + request.getName() + " already exists.");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        roleRepository.save(role);
    }

    @Override
    public void updateRole(Integer id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role with id " + id + " not found."));

        if (request.getName() != null && !request.getName().equals(role.getName())) {
            if (roleRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Role with name " + request.getName() + " already exists.");
            }

            role.setName(request.getName());
        }

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        roleRepository.save(role);
    }

    @Override
    public void deleteRole(Integer id) {
        if (!roleRepository.existsById(id)) {
            throw new IllegalArgumentException("Role with id " + id + " not found.");
        }

        roleRepository.deleteById(id);
    }


}
