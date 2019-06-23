package com.apartmentmanager.dao;

import com.apartmentmanager.constant.CustomerStatus;
import com.apartmentmanager.po.customer.CustomerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerDao extends JpaRepository<CustomerInfo, Integer> {

    @Query("from CustomerInfo c where c.apartment.id = ?1 and c.status = com.apartmentmanager.constant.CustomerStatus.ENROLLED")
    public List<CustomerInfo> findCurrentCustomersByApartmentId(Integer apartmentId);

    @Modifying
    @Query("update CustomerInfo c set c.status = ?1 where c.id in ?2")
    public void updateStatusByCustomerIds(CustomerStatus status, List<Integer> customerIds);

}
