package com.cash.cash.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cash.cash.model.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

}