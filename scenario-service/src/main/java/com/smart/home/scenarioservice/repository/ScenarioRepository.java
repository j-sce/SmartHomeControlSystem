package com.smart.home.scenarioservice.repository;

import com.smart.home.scenarioservice.repository.model.ScenarioDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<ScenarioDAO, Long> {

    List<ScenarioDAO> findByDeviceTypeId(Long deviceTypeId);

}
