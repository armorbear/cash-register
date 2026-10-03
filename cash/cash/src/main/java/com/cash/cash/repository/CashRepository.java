package com.cash.cash.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cash.cash.model.Cash;

public interface CashRepository extends JpaRepository<Cash, Long> {

    Optional<Cash> findByCode(String code);

}