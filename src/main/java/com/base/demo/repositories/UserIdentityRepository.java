package com.base.demo.repositories;

import com.base.demo.constants.enums.user.UserProvider;
import com.base.demo.entities.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentity, Integer> {

    Optional<UserIdentity> findByProviderAndProviderUserId(UserProvider provider, String providerUserId);

    Optional<UserIdentity> findByUserId(Integer userId);
}
