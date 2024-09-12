package com.smart.home.deviceservice.service;

import com.smart.home.deviceservice.model.DeviceType;

import java.util.List;
import java.util.Optional;

public interface DeviceTypeService {

    DeviceType addDeviceType(String deviceTypeName);

    DeviceType updateDeviceTypeById(Long id, String deviceTypeUpdate);

    void deleteDeviceTypeById(Long deviceTypeId);

    DeviceType getDeviceTypeById(Long deviceTypeId);

    List<DeviceType> getAllDeviceTypes();

}
