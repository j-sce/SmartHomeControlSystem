package com.smart.home.deviceservice.integration;

import com.smart.home.deviceservice.cache.CacheConfig;
import com.smart.home.deviceservice.mapper.DeviceTypeMapper;
import com.smart.home.deviceservice.mapper.DeviceTypeMapperImpl;
import com.smart.home.deviceservice.model.DeviceType;
import com.smart.home.deviceservice.repository.DeviceTypeRepository;
import com.smart.home.deviceservice.repository.model.DeviceTypeDAO;
import com.smart.home.deviceservice.service.DeviceTypeService;
import com.smart.home.deviceservice.service.impl.DeviceTypeServiceImpl;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import({CacheConfig.class, DeviceTypeServiceImpl.class, DeviceTypeMapperImpl.class})
@ExtendWith(SpringExtension.class)
@EnableCaching
@ImportAutoConfiguration(classes = {
        CacheAutoConfiguration.class,
        RedisAutoConfiguration.class
})
public class DeviceTypeCachingIntegrationTest {

    @MockBean
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private DeviceTypeService deviceTypeService;

    @Autowired
    private DeviceTypeMapper deviceTypeMapper;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache("deviceType")).clear();
    }


    @Test
    void whenFindDeviceTypeById_thenItemReturnedFromCache_Success() {
        DeviceType deviceType = new DeviceType(1L, "Thermostat");
        DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO(1L, "Thermostat");

        when(deviceTypeRepository.findById(1L)).thenReturn(Optional.of(deviceTypeDAO));

        DeviceType cacheMiss = deviceTypeService.getDeviceTypeById(1L);
        DeviceType cacheHit = deviceTypeService.getDeviceTypeById(1L);

        assertEquals(cacheMiss, deviceType);
        assertEquals(cacheHit, deviceType);

        verify(deviceTypeRepository, times(1)).findById(anyLong());
        assertEquals(Objects.requireNonNull(cacheManager.getCache("deviceType")).get(1L).get(), deviceType);
    }

    @Test
    void whenUpdateDeviceType_thenCacheUpdated_Success() {
        DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO(1L, "Thermostat");
        DeviceType updatedDeviceType = new DeviceType(1L, "Smart Thermostat");
        DeviceTypeDAO updatedDeviceTypeDAO = new DeviceTypeDAO(1L, "Smart Thermostat");

        when(deviceTypeRepository.findById(1L)).thenReturn(Optional.of(deviceTypeDAO));
        when(deviceTypeRepository.save(any(DeviceTypeDAO.class))).thenReturn(updatedDeviceTypeDAO);

        deviceTypeService.updateDeviceTypeById(1L, "Smart Thermostat");

        assertEquals(Objects.requireNonNull(Objects.requireNonNull(cacheManager.getCache("deviceType")).get(1L)).get(), updatedDeviceType);

        DeviceType cacheHit = deviceTypeService.getDeviceTypeById(1L);
        assertEquals(cacheHit, updatedDeviceType);

        verify(deviceTypeRepository, times(1)).findById(1L);
        verify(deviceTypeRepository, times(1)).save(any(DeviceTypeDAO.class));
    }

    @Test
    void whenDeleteDeviceType_thenCacheEvicted_Success() {
        DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO(1L, "Thermostat");

        when(deviceTypeRepository.findById(1L)).thenReturn(Optional.of(deviceTypeDAO));
        doNothing().when(deviceTypeRepository).delete(deviceTypeDAO);

        deviceTypeService.getDeviceTypeById(1L); // populate cache
        deviceTypeService.deleteDeviceTypeById(1L);

        assertNull(cacheManager.getCache("deviceType").get(1L));

        verify(deviceTypeRepository, times(2)).findById(1L);
        verify(deviceTypeRepository, times(1)).delete(deviceTypeDAO);
    }

    @Test
    void whenFindNonExistentDeviceType_thenCacheNotPopulated_Failure() {
        when(deviceTypeRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            deviceTypeService.getDeviceTypeById(999L);
        } catch (Exception ignored) {
        }

        assertNull(cacheManager.getCache("deviceType").get(999L));
        verify(deviceTypeRepository, times(1)).findById(999L);
    }

    @Test
    void whenUpdateNonExistentDeviceType_thenCacheNotUpdated_Failure() {
        when(deviceTypeRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            deviceTypeService.updateDeviceTypeById(999L, "Non-Existent");
        } catch (Exception ignored) {
        }

        assertNull(cacheManager.getCache("deviceType").get(999L));
        verify(deviceTypeRepository, times(1)).findById(999L);
    }

}
