package com.smart.home.deviceservice.service;

import com.smart.home.deviceservice.model.DeviceStatusChange;

public interface DeviceStatusChangeService {

    DeviceStatusChange changeDeviceStatus(Long deviceId, String newStatus, String weatherCondition, Long scenarioId);

}
