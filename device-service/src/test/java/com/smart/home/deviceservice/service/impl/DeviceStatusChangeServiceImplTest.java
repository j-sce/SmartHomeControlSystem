package com.smart.home.deviceservice.service.impl;

import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.mapper.DeviceStatusChangeMapper;
import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.model.DeviceStatusChange;
import com.smart.home.deviceservice.repository.DeviceStatusChangeRepository;
import com.smart.home.deviceservice.repository.model.DeviceStatusChangeDAO;
import com.smart.home.deviceservice.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceStatusChangeServiceImplTest {

    @Mock
    private DeviceService deviceService;

    @Mock
    private DeviceStatusChangeRepository statusChangeRepository;

    @Mock
    private DeviceStatusChangeMapper deviceStatusChangeMapper;

    @InjectMocks
    private DeviceStatusChangeServiceImpl deviceStatusChangeService;


    @Test
    void changeDeviceStatus_Success() {
        Long deviceId = 1L;
        String newStatus = "ON";
        String weatherCondition = "Clear";
        Long scenarioId = 100L;

        Device device = new Device();
        device.setDeviceId(1L);
        device.setStatus("OFF");
        when(deviceService.getDeviceById(deviceId)).thenReturn(device);
        when(deviceService.updateDeviceById(deviceId, device)).thenReturn(device);

        DeviceStatusChange statusChange = new DeviceStatusChange(null, deviceId, "OFF", newStatus, null, weatherCondition, scenarioId);
        DeviceStatusChangeDAO statusChangeDAO = new DeviceStatusChangeDAO();
        when(deviceStatusChangeMapper.deviceStatusChangeToDeviceStatusChangeDAO(any(DeviceStatusChange.class))).thenReturn(statusChangeDAO);
        when(statusChangeRepository.save(statusChangeDAO)).thenReturn(statusChangeDAO);
        when(deviceStatusChangeMapper.deviceStatusChangeDAOToDeviceStatusChange(statusChangeDAO)).thenReturn(statusChange);

        DeviceStatusChange result = deviceStatusChangeService.changeDeviceStatus(deviceId, newStatus, weatherCondition, scenarioId);

        assertNotNull(result);
        assertEquals(newStatus, result.getNewStatus());
        verify(deviceService, times(1)).getDeviceById(deviceId);
        verify(deviceService, times(1)).updateDeviceById(deviceId, device);
        verify(statusChangeRepository, times(1)).save(statusChangeDAO);
        verify(deviceStatusChangeMapper, times(1)).deviceStatusChangeDAOToDeviceStatusChange(statusChangeDAO);
    }

    @Test
    void changeDeviceStatus_DeviceNotFound_ThrowsBadRequestException() {
        Long deviceId = 1L;
        when(deviceService.getDeviceById(deviceId)).thenThrow(new BadRequestException("Device not found with id: 1"));

        assertThrows(BadRequestException.class, () -> deviceStatusChangeService.changeDeviceStatus(deviceId, "ON", "Clear", 100L));
        verify(deviceService, times(1)).getDeviceById(deviceId);
    }

    @Test
    void changeDeviceStatus_StatusUnchanged_ThrowsBadRequestException() {
        Long deviceId = 1L;
        String currentStatus = "ON";
        Device device = new Device();
        device.setStatus(currentStatus);
        when(deviceService.getDeviceById(deviceId)).thenReturn(device);

        assertThrows(BadRequestException.class, () -> deviceStatusChangeService.changeDeviceStatus(deviceId, currentStatus, "Clear", 100L));
        verify(deviceService, times(1)).getDeviceById(deviceId);
    }

}