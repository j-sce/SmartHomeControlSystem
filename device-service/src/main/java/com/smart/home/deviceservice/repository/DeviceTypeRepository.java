package com.smart.home.deviceservice.repository;

import com.smart.home.deviceservice.repository.model.DeviceTypeDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceTypeDAO, Long> {
}
