package com.base.demo.repositories;

import com.base.demo.constants.enums.deposit_request.DepositRequestStatus;
import com.base.demo.entities.DepositRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DepositRequestRepository extends JpaRepository<DepositRequest, Long> {
    boolean existsByUserIdAndStatus(Long userId, DepositRequestStatus status);

    @Query("SELECT d.userId FROM DepositRequest d WHERE d.id = :id")
    Long findUserIdById(Long id);
}
