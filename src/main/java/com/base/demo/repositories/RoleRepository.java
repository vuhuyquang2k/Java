package com.base.demo.repositories;

import com.base.demo.entities.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("""
            select r from Role r
            where (:role is null or r.name like concat('%', :role, '%'))
            and (:description is null or r.description like concat('%', :description, '%'))
            """)
    List<Role> findByFilters(String role, String description, Pageable pageable);

    boolean existsByName(String name);
}
