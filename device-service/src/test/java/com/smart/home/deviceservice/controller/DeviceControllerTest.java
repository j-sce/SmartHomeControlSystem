package com.smart.home.deviceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.handler.GlobalExceptionHandler;
import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    private static final String URL = "/api/device";

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private DeviceController deviceController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Device validDevice;
    private Device deviceWithId;
    private Device invalidDevice;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(deviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void initializeDevices() {
        validDevice = new Device();
        validDevice.setDeviceName("Smart Light");
        validDevice.setDeviceType(1L);
        validDevice.setLatitude(56.9710);
        validDevice.setLongitude(24.1604);
        validDevice.setStatus("OFF");

        deviceWithId = new Device();
        deviceWithId.setDeviceId(1L);
        deviceWithId.setDeviceName("Kitchen Thermostat");
        deviceWithId.setDeviceType(1L);
        deviceWithId.setLatitude(56.9710);
        deviceWithId.setLongitude(24.1604);
        deviceWithId.setStatus("ON");

        invalidDevice = new Device();
    }

    @Test
    void testAddDevice_Success() throws Exception {
        when(deviceService.addDevice(any(Device.class))).thenReturn(deviceWithId);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceWithId)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceId", is(1)))
                .andExpect(jsonPath("$.deviceName", is("Kitchen Thermostat")))
                .andExpect(jsonPath("$.deviceType", is(1)))
                .andExpect(jsonPath("$.latitude", is(56.9710)))
                .andExpect(jsonPath("$.longitude", is(24.1604)))
                .andExpect(jsonPath("$.status", is("ON")));

        verify(deviceService, times(1)).addDevice(any(Device.class));
    }

    @Test
    void testAddDevice_ValidationError() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDevice)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[*]", containsInAnyOrder(
                        "deviceName: Device name must not be blank",
                        "deviceType: Device type id must not be null",
                        "status: Device status must not be blank",
                        "latitude: Latitude values range between -90 and +90 degrees",
                        "longitude: Longitude values range between -180 and +180 degrees")));

        verify(deviceService, never()).addDevice(any(Device.class));
    }

    @Test
    void testUpdateDeviceById_Success() throws Exception {
        when(deviceService.updateDeviceById(anyLong(), any(Device.class))).thenReturn(validDevice);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDevice)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceName", is("Smart Light")))
                .andExpect(jsonPath("$.deviceType", is(1)))
                .andExpect(jsonPath("$.latitude", is(56.9710)))
                .andExpect(jsonPath("$.longitude", is(24.1604)))
                .andExpect(jsonPath("$.status", is("OFF")));

        verify(deviceService, times(1)).updateDeviceById(anyLong(), any(Device.class));
    }

    @Test
    void testUpdateDeviceById_ValidationError() throws Exception {
        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDevice)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[*]", containsInAnyOrder(
                        "deviceName: Device name must not be blank",
                        "deviceType: Device type id must not be null",
                        "status: Device status must not be blank",
                        "latitude: Latitude values range between -90 and +90 degrees",
                        "longitude: Longitude values range between -180 and +180 degrees")));

        verify(deviceService, times(0)).updateDeviceById(anyLong(), any(Device.class));
    }

    @Test
    void testDeleteDeviceById_Success() throws Exception {
        doNothing().when(deviceService).deleteDeviceById(1L);

        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(deviceService, times(1)).deleteDeviceById(1L);
    }

    @Test
    void testDeleteDeviceById_NotFound() throws Exception {
        doThrow(new BadRequestException("Device not found with ID: 1")).when(deviceService).deleteDeviceById(1L);

        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Device not found with ID: 1"));

        verify(deviceService, times(1)).deleteDeviceById(1L);
    }

    @Test
    void testGetDeviceById_Success() throws Exception {
        when(deviceService.getDeviceById(anyLong())).thenReturn(deviceWithId);

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceId", is(1)))
                .andExpect(jsonPath("$.deviceName", is("Kitchen Thermostat")))
                .andExpect(jsonPath("$.deviceType", is(1)))
                .andExpect(jsonPath("$.latitude", is(56.9710)))
                .andExpect(jsonPath("$.longitude", is(24.1604)))
                .andExpect(jsonPath("$.status", is("ON")));

        verify(deviceService, times(1)).getDeviceById(anyLong());
    }

    @Test
    void testGetDeviceById_NotFound() throws Exception {
        when(deviceService.getDeviceById(anyLong())).thenThrow(new BadRequestException("Device not found with id: 1"));

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device not found with id: 1"));

        verify(deviceService, times(1)).getDeviceById(anyLong());
    }

    @Test
    void testGetAllDevices_Success() throws Exception {
        List<Device> deviceList = Arrays.asList(deviceWithId, validDevice);
        when(deviceService.getAllDevices()).thenReturn(deviceList);

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceId").value(deviceWithId.getDeviceId()))
                .andExpect(jsonPath("$[0].deviceName").value(deviceWithId.getDeviceName()))
                .andExpect(jsonPath("$[0].latitude").value(deviceWithId.getLatitude()))
                .andExpect(jsonPath("$[1].deviceId").value(validDevice.getDeviceId()))
                .andExpect(jsonPath("$[1].status").value(validDevice.getStatus()))
                .andExpect(jsonPath("$[1].longitude").value(validDevice.getLongitude()));

        verify(deviceService, times(1)).getAllDevices();
    }

    @Test
    void testGetAllDevices_EmptyList() throws Exception {
        when(deviceService.getAllDevices()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deviceService, times(1)).getAllDevices();
    }

}
