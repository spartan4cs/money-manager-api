package com.opensource.moneymanager.repository;

import com.opensource.moneymanager.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByName(String name);
    List<Account> findByType(String type);
    List<Account> findByIsActiveTrue();
}

