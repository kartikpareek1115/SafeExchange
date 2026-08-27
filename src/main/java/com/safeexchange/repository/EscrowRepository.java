package com.safeexchange.repository;

import com.safeexchange.entity.Escrow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EscrowRepository extends JpaRepository<Escrow, Long> {

    List<Escrow> findByBuyerId(Long buyerId);

    List<Escrow> findBySellerId(Long sellerId);
}