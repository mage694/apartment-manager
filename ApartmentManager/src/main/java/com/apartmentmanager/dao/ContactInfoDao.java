package com.apartmentmanager.dao;

import com.apartmentmanager.po.customer.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactInfoDao extends JpaRepository<ContactInfo, Integer> {

}
