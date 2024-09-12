package com.smart.home.deviceservice.mapper;

import com.smart.home.deviceservice.model.DeviceStatusChange;
import com.smart.home.deviceservice.repository.model.DeviceStatusChangeDAO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceStatusChangeMapper {

    @Mapping(source = "device", target = "device.deviceId")
    DeviceStatusChangeDAO deviceStatusChangeToDeviceStatusChangeDAO(DeviceStatusChange deviceStatusChange);

    @Mapping(source = "device.deviceId", target = "device")
    DeviceStatusChange deviceStatusChangeDAOToDeviceStatusChange(DeviceStatusChangeDAO deviceStatusChangeDAO);

}
