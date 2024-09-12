package com.smart.home.deviceservice.repository;

import com.smart.home.deviceservice.repository.model.DeviceStatusChangeDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceStatusChangeRepository extends JpaRepository<DeviceStatusChangeDAO, Long> {
}
