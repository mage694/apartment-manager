package com.apartmentmanager.dao;

import com.apartmentmanager.constant.ApartmentStatus;
import com.apartmentmanager.po.apartment.ApartmentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ApartmentDao extends JpaRepository<ApartmentInfo, Integer>, QuerydslPredicateExecutor<ApartmentInfo> {
    @Modifying
    @Query("update ApartmentInfo a set a.status=?1 where a.id=?2")
    void updateStatusByApartmentId(ApartmentStatus status, Integer apartmentId);

    @Modifying
    @Query("update ApartmentInfo a set a.status = ?1 where a.id = ?2")
    void updateApartmentStatus(ApartmentStatus status, Integer apartmentId);

}
