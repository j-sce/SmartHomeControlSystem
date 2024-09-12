package com.smart.home.deviceservice.service;

import com.smart.home.deviceservice.model.Device;

import java.util.List;

public interface DeviceService {

    Device addDevice(Device device);

    Device updateDeviceById(Long id, Device deviceUpdate);

    void deleteDeviceById(Long deviceId);

    Device getDeviceById(Long deviceId);

    List<Device> getAllDevices();

}
