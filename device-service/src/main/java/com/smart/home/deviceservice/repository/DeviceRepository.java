package com.smart.home.deviceservice.repository;

import com.smart.home.deviceservice.repository.model.DeviceDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceDAO, Long> {
}
