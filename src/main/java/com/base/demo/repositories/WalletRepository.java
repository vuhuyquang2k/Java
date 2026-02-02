package com.base.demo.repositories;

import com.base.demo.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    boolean existsByUserId(Integer userId);
    Wallet findByUserId(Integer userId);
}
