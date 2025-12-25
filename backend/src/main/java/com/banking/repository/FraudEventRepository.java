package com.banking.repository;

import com.banking.entity.FraudEvent;
import com.banking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FraudEventRepository extends JpaRepository<FraudEvent, UUID> {
    List<FraudEvent> findByUserOrderByTimestampDesc(User user);
    List<FraudEvent> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime start, LocalDateTime end);
    List<FraudEvent> findAllByOrderByTimestampDesc();
}

