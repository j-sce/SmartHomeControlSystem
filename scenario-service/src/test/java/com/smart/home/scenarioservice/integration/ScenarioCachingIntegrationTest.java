package com.smart.home.scenarioservice.integration;

import com.smart.home.scenarioservice.cache.CacheConfig;
import com.smart.home.scenarioservice.client.DeviceTypeClient;
import com.smart.home.scenarioservice.mapper.ScenarioMapper;
import com.smart.home.scenarioservice.mapper.ScenarioMapperImpl;
import com.smart.home.scenarioservice.model.Scenario;
import com.smart.home.scenarioservice.model.dto.DeviceTypeDTO;
import com.smart.home.scenarioservice.repository.ScenarioRepository;
import com.smart.home.scenarioservice.repository.model.ScenarioDAO;
import com.smart.home.scenarioservice.service.ScenarioService;
import com.smart.home.scenarioservice.service.ScenarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import({CacheConfig.class, ScenarioServiceImpl.class, ScenarioMapperImpl.class})
@ExtendWith(SpringExtension.class)
@EnableCaching
@ImportAutoConfiguration(classes = {
        CacheAutoConfiguration.class,
        RedisAutoConfiguration.class
})
public class ScenarioCachingIntegrationTest {

    @MockBean
    private ScenarioRepository scenarioRepository;

    @MockBean
    private DeviceTypeClient deviceTypeClient;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private ScenarioMapper scenarioMapper;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache("scenario")).clear();
    }


    @Test
    void whenFindScenarioById_thenItemReturnedFromCache_Success() {
        Scenario scenario = new Scenario(1L, 1L, "temperature", "25", ">", "ON");
        ScenarioDAO scenarioDAO = new ScenarioDAO(1L, 1L, "temperature", "25", ">", "ON");

        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString())).thenReturn(new DeviceTypeDTO());
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenarioDAO));

        Scenario cacheMiss = scenarioService.getScenarioById(1L);
        Scenario cacheHit = scenarioService.getScenarioById(1L);

        assertEquals(cacheMiss, scenario);
        assertEquals(cacheHit, scenario);

        verify(scenarioRepository, times(1)).findById(anyLong());
    }

    @Test
    void whenUpdateScenario_thenCacheUpdated_Success() {
        ScenarioDAO scenarioDAO = new ScenarioDAO(1L, 1L, "temperature", "25", ">", "ON");
        Scenario updatedScenario = new Scenario(1L, 1L, "temperature", "25", ">", "OFF");
        ScenarioDAO updatedScenarioDAO = new ScenarioDAO(1L, 1L, "temperature", "25", ">", "OFF");

        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString())).thenReturn(new DeviceTypeDTO());
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenarioDAO));
        when(scenarioRepository.save(any(ScenarioDAO.class))).thenReturn(updatedScenarioDAO);

        scenarioService.updateScenarioById(1L, updatedScenario, "token");

        assertEquals(Objects.requireNonNull(Objects.requireNonNull(cacheManager.getCache("scenario")).get(1L)).get(), updatedScenario);

        Scenario cacheHit = scenarioService.getScenarioById(1L);
        assertEquals(cacheHit, updatedScenario);

        verify(scenarioRepository, times(1)).findById(1L);
        verify(scenarioRepository, times(1)).save(any(ScenarioDAO.class));
    }

    @Test
    void whenDeleteScenario_thenCacheEvicted_Success() {
        ScenarioDAO scenarioDAO = new ScenarioDAO(1L, 1L, "temperature", "25", ">", "ON");;

        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString())).thenReturn(new DeviceTypeDTO());
        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenarioDAO));
        doNothing().when(scenarioRepository).delete(scenarioDAO);

        scenarioService.getScenarioById(1L); // populate cache
        scenarioService.deleteScenarioById(1L);

        assertNull(cacheManager.getCache("scenario").get(1L));

        verify(scenarioRepository, times(2)).findById(1L);
        verify(scenarioRepository, times(1)).delete(scenarioDAO);
    }

    @Test
    void whenFindNonExistentScenario_thenCacheNotPopulated_Failure() {
        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString())).thenReturn(new DeviceTypeDTO());
        when(scenarioRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            scenarioService.getScenarioById(999L);
        } catch (Exception ignored) {
        }

        assertNull(cacheManager.getCache("scenario").get(999L));
        verify(scenarioRepository, times(1)).findById(999L);
    }

    @Test
    void whenUpdateNonExistentScenario_thenCacheNotUpdated_Failure() {
        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString())).thenReturn(new DeviceTypeDTO());
        Scenario scenario = new Scenario(1L, 1L, "temperature", "25", ">", "ON");
        when(scenarioRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            scenarioService.updateScenarioById(999L, scenario, "token");
        } catch (Exception ignored) {
        }

        assertNull(cacheManager.getCache("scenario").get(999L));
        verify(scenarioRepository, times(1)).findById(999L);
    }

}
