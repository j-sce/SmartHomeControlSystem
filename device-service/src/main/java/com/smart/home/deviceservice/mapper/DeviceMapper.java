package com.smart.home.deviceservice.mapper;

import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.repository.model.DeviceDAO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(source = "deviceType", target = "deviceType.deviceTypeId")
    DeviceDAO deviceToDeviceDAO(Device device);

    @Mapping(source = "deviceType.deviceTypeId", target = "deviceType")
    Device deviceDAOToDevice(DeviceDAO deviceDAO);

}
