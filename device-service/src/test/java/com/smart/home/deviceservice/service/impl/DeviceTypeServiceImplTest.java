package com.smart.home.deviceservice.service.impl;

import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.mapper.DeviceTypeMapper;
import com.smart.home.deviceservice.model.DeviceType;
import com.smart.home.deviceservice.repository.DeviceTypeRepository;
import com.smart.home.deviceservice.repository.model.DeviceTypeDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
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
class DeviceTypeServiceImplTest {

    @Mock
    private DeviceTypeRepository deviceTypeRepository;

    @Mock
    private DeviceTypeMapper deviceTypeMapper;

    @InjectMocks
    private DeviceTypeServiceImpl deviceTypeService;

    @Test
    void addDeviceType_Success() {
        String deviceTypeName = "Smart Light";
        DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO(deviceTypeName);
        DeviceType deviceType = new DeviceType();
        deviceType.setDeviceTypeName(deviceTypeName);

        when(deviceTypeRepository.save(any(DeviceTypeDAO.class))).thenReturn(deviceTypeDAO);
        when(deviceTypeMapper.deviceTypeDAOToDeviceType(any(DeviceTypeDAO.class))).thenReturn(deviceType);

        DeviceType result = deviceTypeService.addDeviceType(deviceTypeName);

        assertNotNull(result);
        assertEquals(deviceTypeName, result.getDeviceTypeName());
        verify(deviceTypeRepository, times(1)).save(any(DeviceTypeDAO.class));
        verify(deviceTypeMapper, times(1)).deviceTypeDAOToDeviceType(any(DeviceTypeDAO.class));
    }

    @Test
    void addDeviceType_DataIntegrityViolationException_ThrowsBadRequestException() {
        String deviceTypeName = "Smart Light";
        DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO(deviceTypeName);

        when(deviceTypeRepository.save(deviceTypeDAO)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(BadRequestException.class, () -> deviceTypeService.addDeviceType(deviceTypeName));
        verify(deviceTypeRepository, times(1)).save(deviceTypeDAO);
    }

    @Test
    void addDeviceType_UnexpectedException() {
        String deviceTypeName = "Smart Light";
        DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO(deviceTypeName);

        when(deviceTypeRepository.save(deviceTypeDAO)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> deviceTypeService.addDeviceType(deviceTypeName));
        verify(deviceTypeRepository, times(1)).save(deviceTypeDAO);
    }

    @Test
    void updateDeviceTypeById_Success() {
        Long deviceTypeId = 1L;
        String updatedDeviceTypeName = "Smart Thermostat";
        DeviceTypeDAO existingDeviceTypeDAO = new DeviceTypeDAO("Smart Light");
        DeviceType updatedDeviceType = new DeviceType();
        updatedDeviceType.setDeviceTypeName(updatedDeviceTypeName);

        when(deviceTypeRepository.findById(deviceTypeId)).thenReturn(Optional.of(existingDeviceTypeDAO));
        when(deviceTypeRepository.save(any(DeviceTypeDAO.class))).thenReturn(existingDeviceTypeDAO);
        when(deviceTypeMapper.deviceTypeDAOToDeviceType(existingDeviceTypeDAO)).thenReturn(updatedDeviceType);

        DeviceType result = deviceTypeService.updateDeviceTypeById(deviceTypeId, updatedDeviceTypeName);

        assertNotNull(result);
        assertEquals(updatedDeviceTypeName, result.getDeviceTypeName());
        verify(deviceTypeRepository, times(1)).findById(deviceTypeId);
        verify(deviceTypeRepository, times(1)).save(existingDeviceTypeDAO);
        verify(deviceTypeMapper, times(1)).deviceTypeDAOToDeviceType(existingDeviceTypeDAO);
    }

    @Test
    void updateDeviceTypeById_DeviceTypeNotFound_ThrowsBadRequestException() {
        Long deviceTypeId = 1L;
        String updatedDeviceTypeName = "Smart Thermostat";

        when(deviceTypeRepository.findById(deviceTypeId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> deviceTypeService.updateDeviceTypeById(deviceTypeId, updatedDeviceTypeName));
        verify(deviceTypeRepository, times(1)).findById(deviceTypeId);
    }

    @Test
    void updateDeviceTypeById_DataIntegrityViolationException_ThrowsBadRequestException() {
        Long deviceTypeId = 1L;
        String updatedDeviceTypeName = "Smart Thermostat";
        DeviceTypeDAO existingDeviceTypeDAO = new DeviceTypeDAO("Smart Light");

        when(deviceTypeRepository.findById(deviceTypeId)).thenReturn(Optional.of(existingDeviceTypeDAO));
        when(deviceTypeRepository.save(existingDeviceTypeDAO)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(BadRequestException.class, () -> deviceTypeService.updateDeviceTypeById(deviceTypeId, updatedDeviceTypeName));
        verify(deviceTypeRepository, times(1)).findById(deviceTypeId);
        verify(deviceTypeRepository, times(1)).save(existingDeviceTypeDAO);
    }

    @Test
    void deleteDeviceTypeById_Success() {
        Long deviceTypeId = 1L;
        DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO("Smart Light");

        when(deviceTypeRepository.findById(deviceTypeId)).thenReturn(Optional.of(deviceTypeDAO));

        deviceTypeService.deleteDeviceTypeById(deviceTypeId);

        verify(deviceTypeRepository, times(1)).findById(deviceTypeId);
        verify(deviceTypeRepository, times(1)).delete(deviceTypeDAO);
    }

    @Test
    void deleteDeviceTypeById_DeviceTypeNotFound_ThrowsBadRequestException() {
        Long deviceTypeId = 1L;

        when(deviceTypeRepository.findById(deviceTypeId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> deviceTypeService.deleteDeviceTypeById(deviceTypeId));
        verify(deviceTypeRepository, times(1)).findById(deviceTypeId);
    }

    @Test
    void getDeviceTypeById_Success() {
        Long deviceTypeId = 1L;
        DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO("Smart Light");
        DeviceType deviceType = new DeviceType();
        deviceType.setDeviceTypeName("Smart Light");

        when(deviceTypeRepository.findById(deviceTypeId)).thenReturn(Optional.of(deviceTypeDAO));
        when(deviceTypeMapper.deviceTypeDAOToDeviceType(deviceTypeDAO)).thenReturn(deviceType);

        DeviceType result = deviceTypeService.getDeviceTypeById(deviceTypeId);

        assertEquals(deviceType, result);
        verify(deviceTypeRepository, times(1)).findById(deviceTypeId);
        verify(deviceTypeMapper, times(1)).deviceTypeDAOToDeviceType(deviceTypeDAO);
    }

    @Test
    void getDeviceTypeById_DeviceTypeNotFound_ThrowsBadRequestException() {
        Long deviceTypeId = 1L;

        when(deviceTypeRepository.findById(deviceTypeId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> deviceTypeService.getDeviceTypeById(deviceTypeId));
        verify(deviceTypeRepository, times(1)).findById(deviceTypeId);
    }

    @Test
    void getAllDeviceTypes_Success() {
        DeviceTypeDAO deviceTypeDAO1 = new DeviceTypeDAO("Smart Light");
        DeviceTypeDAO deviceTypeDAO2 = new DeviceTypeDAO("Smart Thermostat");
        DeviceType deviceType1 = new DeviceType();
        deviceType1.setDeviceTypeName("Smart Light");
        DeviceType deviceType2 = new DeviceType();
        deviceType2.setDeviceTypeName("Smart Thermostat");

        when(deviceTypeRepository.findAll()).thenReturn(Arrays.asList(deviceTypeDAO1, deviceTypeDAO2));
        when(deviceTypeMapper.deviceTypeDAOToDeviceType(deviceTypeDAO1)).thenReturn(deviceType1);
        when(deviceTypeMapper.deviceTypeDAOToDeviceType(deviceTypeDAO2)).thenReturn(deviceType2);

        List<DeviceType> result = deviceTypeService.getAllDeviceTypes();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(List.of(deviceType1, deviceType2), result);
        verify(deviceTypeRepository, times(1)).findAll();
        verify(deviceTypeMapper, times(2)).deviceTypeDAOToDeviceType(any(DeviceTypeDAO.class));
    }

}