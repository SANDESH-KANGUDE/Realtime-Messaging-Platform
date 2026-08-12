package com.company.chatplatform.paymentservice.domain.repository;

import com.company.chatplatform.paymentservice.domain.entity.PaymentIntentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntentEntity, String> {
    List<PaymentIntentEntity> findByUserId(String userId);
}
