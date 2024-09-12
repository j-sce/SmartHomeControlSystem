package com.smart.home.deviceservice.controller;

import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.handler.GlobalExceptionHandler;
import com.smart.home.deviceservice.model.DeviceType;
import com.smart.home.deviceservice.service.DeviceTypeService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
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
class DeviceTypeControllerTest {

    private static final String URL = "/api/device/type";

    @Mock
    private DeviceTypeService deviceTypeService;

    @InjectMocks
    private DeviceTypeController deviceTypeController;

    private MockMvc mockMvc;

    private DeviceType validDeviceType;
    private DeviceType deviceTypeWithId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(deviceTypeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @BeforeEach
    void initializeDeviceTypes() {
        validDeviceType = new DeviceType();
        validDeviceType.setDeviceTypeName("Smart Light");

        deviceTypeWithId = new DeviceType();
        deviceTypeWithId.setDeviceTypeId(1L);
        deviceTypeWithId.setDeviceTypeName("Thermostat");
    }

    @Test
    void testGetAllDeviceTypes() throws Exception {
        List<DeviceType> deviceTypeList = Arrays.asList(deviceTypeWithId, validDeviceType);
        when(deviceTypeService.getAllDeviceTypes()).thenReturn(deviceTypeList);

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceTypeId").value(deviceTypeWithId.getDeviceTypeId()))
                .andExpect(jsonPath("$[0].deviceTypeName").value(deviceTypeWithId.getDeviceTypeName()))
                .andExpect(jsonPath("$[1].deviceTypeName").value(validDeviceType.getDeviceTypeName()));

        verify(deviceTypeService, times(1)).getAllDeviceTypes();
    }

    @Test
    void testGetAllDeviceTypes_EmptyList() throws Exception {
        when(deviceTypeService.getAllDeviceTypes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deviceTypeService, times(1)).getAllDeviceTypes();
    }

    @Test
    void testGetDeviceTypeById_Success() throws Exception {
        when(deviceTypeService.getDeviceTypeById(1L)).thenReturn(deviceTypeWithId);

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceTypeId").value(1L))
                .andExpect(jsonPath("$.deviceTypeName").value("Thermostat"));

        verify(deviceTypeService, times(1)).getDeviceTypeById(1L);
    }

    @Test
    void testGetDeviceTypeById_NotFound() throws Exception {
        when(deviceTypeService.getDeviceTypeById(1L)).thenThrow(new BadRequestException("Device type not found with id: 1"));

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Device type not found with id: 1"));

        verify(deviceTypeService, times(1)).getDeviceTypeById(1L);
    }

    @Test
    void testAddDeviceType_Success() throws Exception {
        when(deviceTypeService.addDeviceType(anyString()))
                .thenReturn(deviceTypeWithId);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("deviceType", "Smart Light"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceTypeId").value(deviceTypeWithId.getDeviceTypeId()))
                .andExpect(jsonPath("$.deviceTypeName").value(deviceTypeWithId.getDeviceTypeName()));

        verify(deviceTypeService).addDeviceType(anyString());
    }

    @Test
    void testUpdateDeviceTypeById_Success() throws Exception {
        when(deviceTypeService.updateDeviceTypeById(eq(1L), anyString()))
                .thenReturn(deviceTypeWithId);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("deviceType", "Thermostat"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceTypeName").value(deviceTypeWithId.getDeviceTypeName()));

        verify(deviceTypeService).updateDeviceTypeById(eq(1L), anyString());
    }

    @Test
    void testDeleteDeviceTypeById_Success() throws Exception {
        doNothing().when(deviceTypeService).deleteDeviceTypeById(1L);

        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(deviceTypeService).deleteDeviceTypeById(1L);
    }

    @Test
    void testDeleteDeviceTypeById_NotFound() throws Exception {
        doThrow(new BadRequestException("Device type not found with id: 1")).when(deviceTypeService).deleteDeviceTypeById(1L);

        mockMvc.perform(delete(URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Device type not found with id: 1"));
    }

}