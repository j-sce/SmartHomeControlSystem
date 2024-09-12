package com.smart.home.deviceservice.mapper;

import com.smart.home.deviceservice.model.DeviceType;
import com.smart.home.deviceservice.repository.model.DeviceTypeDAO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeviceTypeMapper {

    DeviceTypeDAO deviceTypeToDeviceTypeDAO(DeviceType deviceType);

    DeviceType deviceTypeDAOToDeviceType(DeviceTypeDAO deviceTypeDAO);

}
