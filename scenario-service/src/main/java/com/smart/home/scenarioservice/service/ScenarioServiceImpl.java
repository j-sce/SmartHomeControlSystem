package com.smart.home.scenarioservice.service;

import com.smart.home.scenarioservice.client.DeviceTypeClient;
import com.smart.home.scenarioservice.handler.BadRequestException;
import com.smart.home.scenarioservice.mapper.ScenarioMapper;
import com.smart.home.scenarioservice.model.Scenario;
import com.smart.home.scenarioservice.repository.ScenarioRepository;
import com.smart.home.scenarioservice.repository.model.ScenarioDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ScenarioServiceImpl implements ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioMapper scenarioMapper;
    private final DeviceTypeClient deviceTypeClient;


    @Override
    public Scenario addScenario(Scenario scenario, String token) {
        log.debug("Adding new scenario: {}", scenario);
        validateDeviceType(scenario.getDeviceTypeId(), token);

        ScenarioDAO scenarioDAO = scenarioMapper.scenarioToScenarioDAO(scenario);
        ScenarioDAO addedScenarioDAO = saveScenarioDAO(scenarioDAO, "adding");
        log.info("Scenario added: {}", addedScenarioDAO);
        return scenarioMapper.scenarioDAOToScenario(addedScenarioDAO);
    }

    @Override
    @CachePut(value = "scenario", key = "#scenarioId")
    public Scenario updateScenarioById(Long scenarioId, Scenario scenarioUpdate, String token) {
        log.debug("Updating scenario with id: {}", scenarioId);
        validateDeviceType(scenarioUpdate.getDeviceTypeId(), token);

        ScenarioDAO existingScenarioDAO = getScenarioDAOById(scenarioId);
        scenarioUpdate.setScenarioId(scenarioId);
        existingScenarioDAO = scenarioMapper.scenarioToScenarioDAO(scenarioUpdate);

        ScenarioDAO updatedScenarioDAO = saveScenarioDAO(existingScenarioDAO, "updating");
        log.info("Scenario updated: {}", updatedScenarioDAO);
        return scenarioMapper.scenarioDAOToScenario(updatedScenarioDAO);
    }

    @Override
    @CacheEvict(value = "scenario", key = "#scenarioId")
    public void deleteScenarioById(Long scenarioId) {
        log.debug("Deleting scenario with id: {}", scenarioId);
        ScenarioDAO scenarioDAO = getScenarioDAOById(scenarioId);

        scenarioRepository.delete(scenarioDAO);
        log.info("Scenario with id {} deleted successfully.", scenarioId);
    }

    @Override
    @Cacheable(value = "scenario", key = "#scenarioId")
    public Scenario getScenarioById(Long scenarioId) {
        log.debug("Getting scenario with id: {}", scenarioId);
        ScenarioDAO scenarioDAO = getScenarioDAOById(scenarioId);

        Scenario scenario = scenarioMapper.scenarioDAOToScenario(scenarioDAO);
        log.info("Scenario with id {} is {}", scenarioId, scenario);
        return scenario;
    }

    @Override
    public List<Scenario> getAllScenarios() {
        List<ScenarioDAO> scenarioDAOList = scenarioRepository.findAll();
        log.info("Getting scenario list. Size is: {}", scenarioDAOList.size());

        return scenarioDAOList.stream()
                .map(scenarioMapper::scenarioDAOToScenario)
                .collect(Collectors.toList());
    }

    @Override
    public List<Scenario> getScenariosByDeviceTypeId(Long deviceTypeId, String token) {
        validateDeviceType(deviceTypeId, token);

        List<ScenarioDAO> scenarioDAOList = scenarioRepository.findByDeviceTypeId(deviceTypeId);
        log.info("Getting scenario list by device type id. Size is: {}", scenarioDAOList.size());

        return scenarioDAOList.stream()
                .map(scenarioMapper::scenarioDAOToScenario)
                .collect(Collectors.toList());
    }


    private ScenarioDAO getScenarioDAOById(Long id) {
        return scenarioRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Scenario not found with id: " + id));
    }

    private ScenarioDAO saveScenarioDAO(ScenarioDAO scenarioDAO, String actionDescription) {
        try {
            return scenarioRepository.save(scenarioDAO);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while {} scenario: {}", actionDescription, scenarioDAO);
            throw new BadRequestException("Invalid data or foreign key constraint violation");
        } catch (Exception e) {
            log.error("Unexpected error while {} scenario: {}", actionDescription, scenarioDAO);
            throw new RuntimeException("Unexpected error while " + actionDescription + " scenario");
        }
    }

    private void validateDeviceType(Long deviceTypeId, String token) {
        try {
            deviceTypeClient.getDeviceTypeById(deviceTypeId, token);
        } catch (Exception e) {
            log.error("Error validating device type with id: {}", deviceTypeId, e);
            throw new BadRequestException("Device Type validation failed for id: " + deviceTypeId);
        }
    }

}
