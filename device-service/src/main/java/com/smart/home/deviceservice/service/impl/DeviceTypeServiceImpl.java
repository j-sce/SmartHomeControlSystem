package com.smart.home.deviceservice.service.impl;

import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.mapper.DeviceTypeMapper;
import com.smart.home.deviceservice.model.DeviceType;
import com.smart.home.deviceservice.repository.DeviceTypeRepository;
import com.smart.home.deviceservice.repository.model.DeviceTypeDAO;
import com.smart.home.deviceservice.service.DeviceTypeService;
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
public class DeviceTypeServiceImpl implements DeviceTypeService {

    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceTypeMapper deviceTypeMapper;


    @Override
    public DeviceType addDeviceType(String deviceTypeName) {
        log.debug("Adding new device type: {}", deviceTypeName);

        DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO(deviceTypeName);

        DeviceTypeDAO addedDeviceTypeDAO = saveDeviceTypeDAO(deviceTypeDAO, "adding");
        log.info("Device type added: {}", addedDeviceTypeDAO);
        return deviceTypeMapper.deviceTypeDAOToDeviceType(addedDeviceTypeDAO);
    }

    @Override
    @CachePut(value = "deviceType", key = "#deviceTypeId")
    public DeviceType updateDeviceTypeById(Long deviceTypeId, String deviceTypeUpdate) {
        log.debug("Updating device type with id: {}", deviceTypeId);
        DeviceTypeDAO existingDeviceTypeDAO = getDeviceTypeDAOById(deviceTypeId);
        existingDeviceTypeDAO.setDeviceTypeName(deviceTypeUpdate);

        DeviceTypeDAO updatedDeviceTypeDAO = saveDeviceTypeDAO(existingDeviceTypeDAO, "updating");
        log.info("Device type updated: {}", updatedDeviceTypeDAO);
        return deviceTypeMapper.deviceTypeDAOToDeviceType(updatedDeviceTypeDAO);
    }

    @Override
    @CacheEvict(value = "deviceType", key = "#deviceTypeId")
    public void deleteDeviceTypeById(Long deviceTypeId) {
        log.debug("Deleting device type with id: {}", deviceTypeId);
        DeviceTypeDAO deviceTypeDAO = getDeviceTypeDAOById(deviceTypeId);

        deviceTypeRepository.delete(deviceTypeDAO);
        log.info("Device type with id {} deleted successfully.", deviceTypeId);
    }

    @Override
    @Cacheable(value = "deviceType", key = "#deviceTypeId")
    public DeviceType getDeviceTypeById(Long deviceTypeId) {
        log.debug("Getting device type with id: {}", deviceTypeId);
        DeviceTypeDAO deviceTypeDAO = getDeviceTypeDAOById(deviceTypeId);

        DeviceType deviceType = deviceTypeMapper.deviceTypeDAOToDeviceType(deviceTypeDAO);
        log.info("Device type with id {} is {}", deviceTypeId, deviceType);
        return deviceType;
    }

    @Override
    public List<DeviceType> getAllDeviceTypes() {
        List<DeviceTypeDAO> deviceTypeDAOList = deviceTypeRepository.findAll();
        log.info("Getting device type list. Size is: {}", deviceTypeDAOList.size());

        return deviceTypeDAOList.stream()
                .map(deviceTypeMapper::deviceTypeDAOToDeviceType)
                .collect(Collectors.toList());
    }


    private DeviceTypeDAO getDeviceTypeDAOById(Long id) {
        return deviceTypeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Device type not found with id: " + id));
    }

    private DeviceTypeDAO saveDeviceTypeDAO(DeviceTypeDAO deviceTypeDAO, String actionDescription) {
        try {
            return deviceTypeRepository.save(deviceTypeDAO);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while {} device type: {}", actionDescription, deviceTypeDAO);
            throw new BadRequestException("Invalid data or foreign key constraint violation");
        } catch (Exception e) {
            log.error("Unexpected error while {} device type: {}", actionDescription, deviceTypeDAO);
            throw new RuntimeException("Unexpected error while " + actionDescription + " device type");
        }
    }

}
