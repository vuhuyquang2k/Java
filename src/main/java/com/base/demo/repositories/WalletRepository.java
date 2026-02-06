package com.base.demo.repositories;

import com.base.demo.constants.enums.wallet.WalletStatus;
import com.base.demo.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByUserId(Long userId);

    Wallet findByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, WalletStatus status);
}
