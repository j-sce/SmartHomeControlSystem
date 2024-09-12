package com.smart.home.scenarioservice.service;

import com.smart.home.scenarioservice.client.DeviceTypeClient;
import com.smart.home.scenarioservice.model.dto.DeviceTypeDTO;
import com.smart.home.scenarioservice.handler.BadRequestException;
import com.smart.home.scenarioservice.mapper.ScenarioMapper;
import com.smart.home.scenarioservice.model.Scenario;
import com.smart.home.scenarioservice.repository.ScenarioRepository;
import com.smart.home.scenarioservice.repository.model.ScenarioDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioServiceImplTest {

    @Mock
    private ScenarioRepository scenarioRepository;

    @Mock
    private ScenarioMapper scenarioMapper;

    @Mock
    private DeviceTypeClient deviceTypeClient;

    @InjectMocks
    private ScenarioServiceImpl scenarioService;

    @Test
    void addScenario_Success() {
        Long deviceTypeId = 1L;
        Scenario scenario = new Scenario();
        scenario.setDeviceTypeId(deviceTypeId);
        ScenarioDAO scenarioDAO = new ScenarioDAO();
        DeviceTypeDTO deviceTypeDTO = new DeviceTypeDTO();
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenReturn(deviceTypeDTO);
        when(scenarioMapper.scenarioToScenarioDAO(scenario)).thenReturn(scenarioDAO);
        when(scenarioRepository.save(any(ScenarioDAO.class))).thenReturn(scenarioDAO);
        when(scenarioMapper.scenarioDAOToScenario(scenarioDAO)).thenReturn(scenario);

        Scenario result = scenarioService.addScenario(scenario, token);

        assertNotNull(result);
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(1)).save(scenarioDAO);
        verify(scenarioMapper, times(1)).scenarioDAOToScenario(scenarioDAO);
    }

    @Test
    void addScenario_DataIntegrityViolationException_ThrowsBadRequestException() {
        Long deviceTypeId = 1L;
        Scenario scenario = new Scenario();
        scenario.setDeviceTypeId(deviceTypeId);
        ScenarioDAO scenarioDAO = new ScenarioDAO();
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenReturn(new DeviceTypeDTO());
        when(scenarioMapper.scenarioToScenarioDAO(scenario)).thenReturn(scenarioDAO);
        when(scenarioRepository.save(scenarioDAO)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(BadRequestException.class, () -> scenarioService.addScenario(scenario, token));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(1)).save(scenarioDAO);
    }

    @Test
    void addScenario_UnexpectedException() {
        Long deviceTypeId = 1L;
        Scenario scenario = new Scenario();
        scenario.setDeviceTypeId(deviceTypeId);
        ScenarioDAO scenarioDAO = new ScenarioDAO();
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenReturn(new DeviceTypeDTO());
        when(scenarioMapper.scenarioToScenarioDAO(scenario)).thenReturn(scenarioDAO);
        when(scenarioRepository.save(scenarioDAO)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> scenarioService.addScenario(scenario, token));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(1)).save(scenarioDAO);
    }

    @Test
    void addScenario_InvalidDeviceType_ThrowsBadRequestException() {
        Long deviceTypeId = 1L;
        Scenario scenario = new Scenario();
        scenario.setDeviceTypeId(deviceTypeId);
        ScenarioDAO scenarioDAO = new ScenarioDAO();
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> scenarioService.addScenario(scenario, token));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(0)).save(scenarioDAO);
    }

    @Test
    void updateScenarioById_Success() {
        Long scenarioId = 1L;
        Long deviceTypeId = 1L;
        Scenario scenarioUpdate = new Scenario();
        scenarioUpdate.setDeviceTypeId(deviceTypeId);
        ScenarioDAO scenarioDAO = new ScenarioDAO();
        DeviceTypeDTO deviceTypeDTO = new DeviceTypeDTO();
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenReturn(deviceTypeDTO);
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(scenarioDAO));
        when(scenarioMapper.scenarioToScenarioDAO(scenarioUpdate)).thenReturn(scenarioDAO);
        when(scenarioRepository.save(scenarioDAO)).thenReturn(scenarioDAO);
        when(scenarioMapper.scenarioDAOToScenario(scenarioDAO)).thenReturn(scenarioUpdate);

        Scenario result = scenarioService.updateScenarioById(scenarioId, scenarioUpdate, token);

        assertNotNull(result);
        assertEquals(scenarioUpdate, result);
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(1)).findById(scenarioId);
        verify(scenarioRepository, times(1)).save(scenarioDAO);
        verify(scenarioMapper, times(1)).scenarioDAOToScenario(scenarioDAO);
    }

    @Test
    void updateScenarioById_ScenarioNotFound_ThrowsBadRequestException() {
        Long scenarioId = 1L;
        Long deviceTypeId = 1L;
        Scenario scenarioUpdate = new Scenario();
        scenarioUpdate.setDeviceTypeId(deviceTypeId);
        DeviceTypeDTO deviceTypeDTO = new DeviceTypeDTO();
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenReturn(deviceTypeDTO);
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> scenarioService.updateScenarioById(scenarioId, scenarioUpdate, token));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(1)).findById(scenarioId);
    }

    @Test
    void updateScenarioById_DataIntegrityViolationException_ThrowsBadRequestException() {
        Long scenarioId = 1L;
        Long deviceTypeId = 1L;
        Scenario scenarioUpdate = new Scenario();
        scenarioUpdate.setDeviceTypeId(deviceTypeId);
        ScenarioDAO scenarioDAO = new ScenarioDAO();
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenReturn(new DeviceTypeDTO());
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(scenarioDAO));
        when(scenarioMapper.scenarioToScenarioDAO(scenarioUpdate)).thenReturn(scenarioDAO);
        when(scenarioRepository.save(scenarioDAO)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(BadRequestException.class, () -> scenarioService.updateScenarioById(scenarioId, scenarioUpdate, token));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(1)).findById(scenarioId);
        verify(scenarioRepository, times(1)).save(scenarioDAO);
    }

    @Test
    void updateScenarioById_InvalidDeviceType() {
        Long scenarioId = 1L;
        Long deviceTypeId = 1L;
        Scenario scenarioUpdate = new Scenario();
        scenarioUpdate.setDeviceTypeId(deviceTypeId);
        ScenarioDAO scenarioDAO = new ScenarioDAO();
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> scenarioService.updateScenarioById(scenarioId, scenarioUpdate, token));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(1)).findById(scenarioId);
        verify(scenarioRepository, times(0)).save(scenarioDAO);
    }

    @Test
    void deleteScenarioById_Success() {
        Long scenarioId = 1L;
        ScenarioDAO scenarioDAO = new ScenarioDAO();

        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(scenarioDAO));
        scenarioService.deleteScenarioById(scenarioId);

        verify(scenarioRepository, times(1)).findById(scenarioId);
        verify(scenarioRepository, times(1)).delete(scenarioDAO);
    }

    @Test
    void deleteScenarioById_ScenarioNotFound_ThrowsBadRequestException() {
        Long scenarioId = 1L;

        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> scenarioService.deleteScenarioById(scenarioId));
        verify(scenarioRepository, times(1)).findById(scenarioId);
    }

    @Test
    void getScenarioById_Success() {
        Long scenarioId = 1L;
        ScenarioDAO scenarioDAO = new ScenarioDAO();
        Scenario scenario = new Scenario();

        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(scenarioDAO));
        when(scenarioMapper.scenarioDAOToScenario(scenarioDAO)).thenReturn(scenario);
        Scenario result = scenarioService.getScenarioById(scenarioId);

        assertEquals(scenario, result);
        verify(scenarioRepository, times(1)).findById(scenarioId);
        verify(scenarioMapper, times(1)).scenarioDAOToScenario(scenarioDAO);
    }

    @Test
    void getScenarioById_ScenarioNotFound_ThrowsBadRequestException() {
        Long scenarioId = 1L;

        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> scenarioService.getScenarioById(scenarioId));
        verify(scenarioRepository, times(1)).findById(scenarioId);
    }

    @Test
    void getAllScenarios_Success() {
        ScenarioDAO scenarioDAO1 = new ScenarioDAO();
        ScenarioDAO scenarioDAO2 = new ScenarioDAO();
        Scenario scenario1 = new Scenario();
        Scenario scenario2 = new Scenario();

        when(scenarioRepository.findAll()).thenReturn(Arrays.asList(scenarioDAO1, scenarioDAO2));
        when(scenarioMapper.scenarioDAOToScenario(scenarioDAO1)).thenReturn(scenario1);
        when(scenarioMapper.scenarioDAOToScenario(scenarioDAO2)).thenReturn(scenario2);
        List<Scenario> result = scenarioService.getAllScenarios();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(List.of(scenario1, scenario2), result);
        verify(scenarioRepository, times(1)).findAll();
        verify(scenarioMapper, times(2)).scenarioDAOToScenario(any(ScenarioDAO.class));
    }

    @Test
    void getScenariosByDeviceTypeId_NoScenariosFound() {
        Long deviceTypeId = 1L;
        DeviceTypeDTO deviceTypeDTO = new DeviceTypeDTO();
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenReturn(deviceTypeDTO);
        when(scenarioRepository.findByDeviceTypeId(deviceTypeId)).thenReturn(Collections.emptyList());

        List<Scenario> result = scenarioService.getScenariosByDeviceTypeId(deviceTypeId, token);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(1)).findByDeviceTypeId(deviceTypeId);
        verify(scenarioMapper, times(0)).scenarioDAOToScenario(any(ScenarioDAO.class));
    }

    @Test
    void getScenariosByDeviceTypeId_InvalidDeviceType_ThrowsBadRequestException() {
        Long deviceTypeId = 1L;
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> scenarioService.getScenariosByDeviceTypeId(deviceTypeId, token));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(0)).findByDeviceTypeId(deviceTypeId);
        verify(scenarioMapper, times(0)).scenarioDAOToScenario(any(ScenarioDAO.class));
    }

    @Test
    void getScenariosByDeviceTypeId_DeviceTypeClientThrowsException_ThrowsBadRequestException() {
        Long deviceTypeId = 1L;
        String token = "Bearer token";

        when(deviceTypeClient.getDeviceTypeById(deviceTypeId, token)).thenThrow(RuntimeException.class);

        assertThrows(BadRequestException.class, () -> scenarioService.getScenariosByDeviceTypeId(deviceTypeId, token));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(deviceTypeId, token);
        verify(scenarioRepository, times(0)).findByDeviceTypeId(deviceTypeId);
        verify(scenarioMapper, times(0)).scenarioDAOToScenario(any(ScenarioDAO.class));
    }

}