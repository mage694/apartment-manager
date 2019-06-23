package com.apartmentmanager.dao;

import com.apartmentmanager.po.enrollment.EnrollmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentHistoryDao extends JpaRepository<EnrollmentHistory, Integer> {

    public EnrollmentHistory findByApartmentIdAndCustomerId(Integer apartmentId, Integer customerId);

}
