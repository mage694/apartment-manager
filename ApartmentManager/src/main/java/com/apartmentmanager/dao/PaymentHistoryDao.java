package com.apartmentmanager.dao;

import com.apartmentmanager.po.payment.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PaymentHistoryDao extends JpaRepository<PaymentHistory, Integer>, QuerydslPredicateExecutor<PaymentHistory> {

    Optional<PaymentHistory> findTopByApartmentIdAndCustomerIdOrderByCreatedTimeDesc(Integer apartmentId, Integer customerId);

}
