package com.smart.home.deviceservice.service.impl;

import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.mapper.DeviceMapper;
import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.repository.DeviceRepository;
import com.smart.home.deviceservice.repository.model.DeviceDAO;
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
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceServiceImpl deviceControlService;

    @Test
    void addDevice_Success() {
        Device device = new Device();
        DeviceDAO deviceDAO = new DeviceDAO();
        when(deviceMapper.deviceToDeviceDAO(any(Device.class))).thenReturn(deviceDAO);
        when(deviceRepository.save(any(DeviceDAO.class))).thenReturn(deviceDAO);
        when(deviceMapper.deviceDAOToDevice(any(DeviceDAO.class))).thenReturn(device);

        Device result = deviceControlService.addDevice(device);

        assertNotNull(result);
        assertEquals(device, result);
        verify(deviceMapper, times(1)).deviceToDeviceDAO(device);
        verify(deviceRepository, times(1)).save(deviceDAO);
        verify(deviceMapper, times(1)).deviceDAOToDevice(deviceDAO);
    }

    @Test
    void addDevice_DataIntegrityViolationException_ThrowsBadRequestException() {
        Device device = new Device();
        DeviceDAO deviceDAO = new DeviceDAO();
        when(deviceMapper.deviceToDeviceDAO(device)).thenReturn(deviceDAO);
        when(deviceRepository.save(deviceDAO)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(BadRequestException.class, () -> deviceControlService.addDevice(device));
        verify(deviceMapper, times(1)).deviceToDeviceDAO(device);
        verify(deviceRepository, times(1)).save(deviceDAO);
        verify(deviceMapper, times(0)).deviceDAOToDevice(any(DeviceDAO.class));
    }

    @Test
    void addDevice_UnexpectedException() {
        Device device = new Device();
        DeviceDAO deviceDAO = new DeviceDAO();
        when(deviceMapper.deviceToDeviceDAO(device)).thenReturn(deviceDAO);
        when(deviceRepository.save(deviceDAO)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> deviceControlService.addDevice(device));
        verify(deviceMapper, times(1)).deviceToDeviceDAO(device);
        verify(deviceRepository, times(1)).save(deviceDAO);
        verify(deviceMapper, times(0)).deviceDAOToDevice(any(DeviceDAO.class));
    }

    @Test
    void updateDeviceById_Success() {
        Long deviceId = 1L;
        Device device = new Device();
        DeviceDAO deviceDAO = new DeviceDAO();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(deviceDAO));
        when(deviceMapper.deviceToDeviceDAO(any(Device.class))).thenReturn(deviceDAO);
        when(deviceRepository.save(any(DeviceDAO.class))).thenReturn(deviceDAO);
        when(deviceMapper.deviceDAOToDevice(any(DeviceDAO.class))).thenReturn(device);

        Device result = deviceControlService.updateDeviceById(deviceId, device);

        assertNotNull(result);
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, times(1)).deviceToDeviceDAO(device);
        verify(deviceRepository, times(1)).save(deviceDAO);
        verify(deviceMapper, times(1)).deviceDAOToDevice(deviceDAO);
    }

    @Test
    void updateDeviceById_DeviceNotFound_ThrowsBadRequestException() {
        Long deviceId = 1L;
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> deviceControlService.updateDeviceById(deviceId, new Device()));
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
    }

    @Test
    void updateDeviceById_DataIntegrityViolationException_ThrowsBadRequestException() {
        Long deviceId = 1L;
        Device device = new Device();
        DeviceDAO existingDeviceDAO = new DeviceDAO();
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDeviceDAO));
        when(deviceMapper.deviceToDeviceDAO(any(Device.class))).thenReturn(existingDeviceDAO);
        when(deviceRepository.save(existingDeviceDAO)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(BadRequestException.class, () -> deviceControlService.updateDeviceById(deviceId, device));
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, times(1)).deviceToDeviceDAO(device);
        verify(deviceRepository, times(1)).save(existingDeviceDAO);
        verify(deviceMapper, times(0)).deviceDAOToDevice(any(DeviceDAO.class));
    }

    @Test
    void deleteDeviceById_Success() {
        Long deviceId = 1L;
        DeviceDAO deviceDAO = new DeviceDAO();
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(deviceDAO));

        deviceControlService.deleteDeviceById(deviceId);

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, times(1)).delete(deviceDAO);
    }

    @Test
    void deleteDeviceById_DeviceNotFound_ThrowsBadRequestException() {
        Long deviceId = 1L;
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> deviceControlService.deleteDeviceById(deviceId));
        verify(deviceRepository, times(1)).findById(deviceId);
    }

    @Test
    void getDeviceById_Success() {
        Long deviceId = 1L;
        Device device = new Device();
        DeviceDAO deviceDAO = new DeviceDAO();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(deviceDAO));
        when(deviceMapper.deviceDAOToDevice(deviceDAO)).thenReturn(device);

        Device result = deviceControlService.getDeviceById(deviceId);

        assertEquals(device, result);
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, times(1)).deviceDAOToDevice(deviceDAO);
    }

    @Test
    void getDeviceById_DeviceNotFound_ThrowsBadRequestException() {
        Long deviceId = 1L;
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> deviceControlService.getDeviceById(deviceId));
        verify(deviceRepository, times(1)).findById(deviceId);
    }

    @Test
    void getAllDevices_Success() {
        DeviceDAO deviceDAO1 = new DeviceDAO();
        DeviceDAO deviceDAO2 = new DeviceDAO();
        Device device1 = new Device();
        Device device2 = new Device();

        when(deviceRepository.findAll()).thenReturn(Arrays.asList(deviceDAO1, deviceDAO2));
        when(deviceMapper.deviceDAOToDevice(deviceDAO1)).thenReturn(device1);
        when(deviceMapper.deviceDAOToDevice(deviceDAO2)).thenReturn(device2);

        List<Device> result = deviceControlService.getAllDevices();

        assertEquals(2, result.size());
        assertEquals(List.of(device1, device2), result);
        verify(deviceRepository, times(1)).findAll();
        verify(deviceMapper, times(2)).deviceDAOToDevice(any(DeviceDAO.class));
    }

}
