package com.base.demo.repositories;

import com.base.demo.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByFullName(String fullName);

    @Query("""
            select u from User u
            where (:fullName is null or u.fullName like concat('%', :fullName, '%'))
            """)
    List<User> findByFilters(String fullName, Pageable pageable);
}
