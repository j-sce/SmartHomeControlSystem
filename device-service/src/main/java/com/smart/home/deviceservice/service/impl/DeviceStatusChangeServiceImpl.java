package com.smart.home.deviceservice.service.impl;

import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.mapper.DeviceStatusChangeMapper;
import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.model.DeviceStatusChange;
import com.smart.home.deviceservice.repository.DeviceStatusChangeRepository;
import com.smart.home.deviceservice.repository.model.DeviceStatusChangeDAO;
import com.smart.home.deviceservice.service.DeviceService;
import com.smart.home.deviceservice.service.DeviceStatusChangeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
public class DeviceStatusChangeServiceImpl implements DeviceStatusChangeService {

    private final DeviceService deviceService;
    private final DeviceStatusChangeRepository statusChangeRepository;
    private final DeviceStatusChangeMapper deviceStatusChangeMapper;


    @Transactional(rollbackOn = {RuntimeException.class, BadRequestException.class})//TODO check if rollback works
    @Override
    public DeviceStatusChange changeDeviceStatus(Long deviceId, String newStatus, String weatherCondition, Long scenarioId) {
        log.debug("Attempting to change status of device with id: {}. New status: {}", deviceId, newStatus);

        Device device = getDevice(deviceId);
        validateStatusChange(device.getStatus(), newStatus);

        DeviceStatusChange statusChange = createDeviceStatusChange(deviceId, device.getStatus(), newStatus, weatherCondition, scenarioId);

        try {
            updateDeviceStatus(device, newStatus);
            DeviceStatusChange result = saveStatusChange(statusChange);
            log.info("Device status change saved: {}", result);
            return result;
        } catch (Exception e){
            log.error("Unexpected error while updating device status and saving device status change.");
            throw new RuntimeException("Unexpected error while updating device status and saving device status change.");
        }
    }

    private Device getDevice(Long deviceId) {
        return deviceService.getDeviceById(deviceId);
    }

    private void validateStatusChange(String oldStatus, String newStatus) {
        if (oldStatus.equals(newStatus)) {
            log.error("Device already has the status '{}'. Status change aborted.", newStatus);
            throw new BadRequestException("Device already has the status '" + newStatus + "'. No change necessary.");
        }
    }

    private void updateDeviceStatus(Device device, String newStatus) {
        device.setStatus(newStatus);
        device.setLastUpdated(LocalDateTime.now());
        deviceService.updateDeviceById(device.getDeviceId(), device);
        log.info("Device status updated. Updated device: {}", device);
    }

    private DeviceStatusChange createDeviceStatusChange(Long deviceId, String oldStatus, String newStatus, String weatherCondition, Long scenarioId) {
        return new DeviceStatusChange(
                null,
                deviceId,
                oldStatus,
                newStatus,
                LocalDateTime.now(),
                weatherCondition,
                scenarioId
        );
    }

    private DeviceStatusChange saveStatusChange(DeviceStatusChange statusChange) {
        DeviceStatusChangeDAO savedChange = statusChangeRepository.save(deviceStatusChangeMapper.deviceStatusChangeToDeviceStatusChangeDAO(statusChange));
        return deviceStatusChangeMapper.deviceStatusChangeDAOToDeviceStatusChange(savedChange);
    }

}
