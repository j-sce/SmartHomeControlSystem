package com.smart.home.deviceservice.service.impl;

import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.mapper.DeviceMapper;
import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.repository.DeviceRepository;
import com.smart.home.deviceservice.repository.model.DeviceDAO;
import com.smart.home.deviceservice.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;


    @Override
    public Device addDevice(Device device) {
        log.debug("Adding new device: {}", device);
        DeviceDAO deviceDAO = deviceMapper.deviceToDeviceDAO(device);
        deviceDAO.setLastUpdated(LocalDateTime.now());
        DeviceDAO addedDeviceDAO = saveDeviceDAO(deviceDAO, "adding");
        log.info("Device added: {}", addedDeviceDAO);
        return deviceMapper.deviceDAOToDevice(addedDeviceDAO);
    }

    @Override
    @CacheEvict(value = "device", key = "#deviceId")
    public Device updateDeviceById(Long deviceId, Device deviceUpdate) {
        log.debug("Updating device with id: {}", deviceId);
        DeviceDAO existingDeviceDAO = getDeviceDAOById(deviceId);
        deviceUpdate.setDeviceId(deviceId);
        deviceUpdate.setLastUpdated(LocalDateTime.now());
        existingDeviceDAO = deviceMapper.deviceToDeviceDAO(deviceUpdate);

        DeviceDAO updatedDeviceDAO = saveDeviceDAO(existingDeviceDAO, "updating");
        log.info("Device updated: {}", updatedDeviceDAO);
        return deviceMapper.deviceDAOToDevice(updatedDeviceDAO);
    }

    @Override
    @CacheEvict(value = "device", key = "#deviceId")
    public void deleteDeviceById(Long deviceId) {
        log.debug("Deleting device with id: {}", deviceId);
        DeviceDAO deviceDAO = getDeviceDAOById(deviceId);

        deviceRepository.delete(deviceDAO);
        log.info("Device with id {} deleted successfully.", deviceId);
    }

    @Override
    @Cacheable(value = "device", key = "#deviceId")
    public Device getDeviceById(Long deviceId) {
        log.debug("Getting device with id: {}", deviceId);
        DeviceDAO deviceDAO = getDeviceDAOById(deviceId);

        Device device = deviceMapper.deviceDAOToDevice(deviceDAO);
        log.info("Device with id {} is {}", deviceId, device);
        return device;
    }

    @Override
    public List<Device> getAllDevices() {
        List<DeviceDAO> deviceDAOList = deviceRepository.findAll();
        log.info("Getting device list. Size is: {}", deviceDAOList.size());

        return deviceDAOList.stream()
                .map(deviceMapper::deviceDAOToDevice)
                .collect(Collectors.toList());
    }


    private DeviceDAO getDeviceDAOById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Device not found with id: " + id));
    }

    private DeviceDAO saveDeviceDAO(DeviceDAO deviceDAO, String actionDescription) {
        try {
            return deviceRepository.save(deviceDAO);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while {} device type: {}", actionDescription, deviceDAO);
            throw new BadRequestException("Invalid data or foreign key constraint violation");
        } catch (Exception e) {
            log.error("Unexpected error while {} device type: {}", actionDescription, deviceDAO);
            throw new RuntimeException("Unexpected error while " + actionDescription + " device type");
        }
    }

}
