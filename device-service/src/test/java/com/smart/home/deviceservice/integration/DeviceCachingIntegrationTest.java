package com.smart.home.deviceservice.integration;

import com.smart.home.deviceservice.cache.CacheConfig;
import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.mapper.DeviceMapper;
import com.smart.home.deviceservice.mapper.DeviceMapperImpl;
import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.repository.DeviceRepository;
import com.smart.home.deviceservice.repository.model.DeviceDAO;
import com.smart.home.deviceservice.repository.model.DeviceTypeDAO;
import com.smart.home.deviceservice.service.DeviceService;
import com.smart.home.deviceservice.service.impl.DeviceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import({CacheConfig.class, DeviceServiceImpl.class, DeviceMapperImpl.class})
@ExtendWith(SpringExtension.class)
@EnableCaching
@ImportAutoConfiguration(classes = {
        CacheAutoConfiguration.class,
        RedisAutoConfiguration.class
})
public class DeviceCachingIntegrationTest {

    @MockBean
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache("device")).clear();
    }


    @Test
    void whenFindDeviceById_thenItemReturnedFromCache_Success() {
        Device device = new Device(1L, "Smart Thermostat", 1L, 56.9710, 24.1604, "ON", LocalDateTime.now());
        DeviceDAO deviceDAO = new DeviceDAO(1L, "Smart Thermostat", new DeviceTypeDAO(1L, "Thermostat"), 56.9710, 24.1604, "ON", LocalDateTime.now());

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(deviceDAO));

        Device cacheMiss = deviceService.getDeviceById(1L);
        Device cacheHit = deviceService.getDeviceById(1L);

        assertEquals(cacheMiss, device);
        assertEquals(cacheHit, device);
        verify(deviceRepository, times(1)).findById(anyLong());
        assertEquals(Objects.requireNonNull(cacheManager.getCache("device")).get(1L).get(), device);
    }

    @Test
    void whenFindDeviceById_thenCacheMissOnInvalidId_Failure() {
        when(deviceRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> deviceService.getDeviceById(2L));
        assertNull(cacheManager.getCache("device").get(2L));
    }

    @Test
    void whenUpdateDevice_thenCacheEvicted_Success() {
        Device deviceUpdate = new Device(1L, "Smart Light", 2L, 57.9710, 25.1604, "OFF", LocalDateTime.now());
        DeviceDAO updatedDeviceDAO = new DeviceDAO(1L, "Smart Light", new DeviceTypeDAO(2L, "Thermostat"), 57.9710, 25.1604, "OFF", LocalDateTime.now());

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(updatedDeviceDAO));
        when(deviceRepository.save(Mockito.any(DeviceDAO.class))).thenReturn(updatedDeviceDAO);

        deviceService.getDeviceById(1L); //to populate cache
        deviceService.updateDeviceById(1L, deviceUpdate);

        assertNull(cacheManager.getCache("device").get(1L));
        verify(deviceRepository, times(1)).save(Mockito.any(DeviceDAO.class));
    }

    @Test
    void whenDeleteDevice_thenCacheEvicted_Success() {
        DeviceDAO deviceDAO = new DeviceDAO(1L, "Smart Thermostat", new DeviceTypeDAO(1L, "Thermostat"), 56.9710, 24.1604, "ON", LocalDateTime.now());

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(deviceDAO));

        deviceService.getDeviceById(1L); //to populate cache
        deviceService.deleteDeviceById(1L);

        verify(deviceRepository, times(1)).delete(deviceDAO);

    }

    @Test
    void whenCacheMiss_thenFallbackToRepository_Success() {
        Device device = new Device(3L, "Smart Camera", 2L, 55.0000, 25.0000, "ON", LocalDateTime.now());
        DeviceDAO deviceDAO = new DeviceDAO(3L, "Smart Camera", new DeviceTypeDAO(2L, "Thermostat"), 55.0000, 25.0000, "ON", LocalDateTime.now());

        when(deviceRepository.findById(3L)).thenReturn(Optional.of(deviceDAO));

        Device cacheMiss = deviceService.getDeviceById(3L);

        assertEquals(device, cacheMiss);
        verify(deviceRepository, times(1)).findById(3L);
        assertEquals(Objects.requireNonNull(cacheManager.getCache("device")).get(3L).get(), device);
    }

}
