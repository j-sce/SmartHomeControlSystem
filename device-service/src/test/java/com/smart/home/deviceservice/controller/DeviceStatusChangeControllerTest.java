package com.smart.home.deviceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.handler.GlobalExceptionHandler;
import com.smart.home.deviceservice.model.DeviceStatusChange;
import com.smart.home.deviceservice.service.DeviceStatusChangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DeviceStatusChangeControllerTest {

    private static final String URL = "/api/device/status";

    @Mock
    private DeviceStatusChangeService deviceStatusChangeService;

    @InjectMocks
    private DeviceStatusChangeController deviceStatusChangeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private DeviceStatusChange deviceStatusChange;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(deviceStatusChangeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void initialize() {
        deviceStatusChange = new DeviceStatusChange();
        deviceStatusChange.setId(1L);
        deviceStatusChange.setDevice(1L);
        deviceStatusChange.setNewStatus("ON");
    }

    @Test
    void testChangeDeviceStatus_Success() throws Exception {
        when(deviceStatusChangeService.changeDeviceStatus(eq(1L), eq("ON"), eq("manual status change"), any()))
                .thenReturn(deviceStatusChange);

        mockMvc.perform(patch(URL + "/{id}", 1L)
                        .param("status", "ON")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(deviceStatusChange)));

        verify(deviceStatusChangeService, times(1)).changeDeviceStatus(eq(1L), eq("ON"), eq("manual status change"), any());
    }

    @Test
    void testChangeDeviceStatus_NotFound() throws Exception {
        doThrow(new BadRequestException("Device not found with ID: 1")).when(deviceStatusChangeService)
                .changeDeviceStatus(eq(1L), eq("ON"), eq("manual status change"), any());

        mockMvc.perform(patch(URL + "/{id}", 1L)
                        .param("status", "ON")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Device not found with ID: 1"));

        verify(deviceStatusChangeService, times(1)).changeDeviceStatus(eq(1L), eq("ON"), eq("manual status change"), any());
    }

}
