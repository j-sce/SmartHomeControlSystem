package com.smart.home.deviceservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.deviceservice.client.AuthClient;
import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.repository.DeviceRepository;
import com.smart.home.deviceservice.repository.model.DeviceDAO;
import com.smart.home.deviceservice.repository.model.DeviceTypeDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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

@SpringBootTest
@ActiveProfiles("localhost")
@AutoConfigureMockMvc
public class DeviceServiceEndToEndTest {

    private static final String URL = "/api/device";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceRepository deviceRepository;

    @MockBean
    private AuthClient authClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    private Device validDevice;
    private DeviceDAO validDeviceDAO;
    private Device deviceWithId;
    private DeviceDAO deviceDAOWithId;
    private Device invalidDevice;
    private String token;

    @BeforeEach
    void clearCache(){
        Objects.requireNonNull(cacheManager.getCache("device")).clear();
    }

    @BeforeEach
    void setUp() {
        validDevice = new Device(
                null,
                "Smart Light",
                1L,
                56.9710,
                24.1604,
                "OFF",
                LocalDateTime.now()
        );

        validDeviceDAO = new DeviceDAO(
                null,
                "Smart Light",
                new DeviceTypeDAO(1L,"Light"),
                56.9710,
                24.1604,
                "OFF",
                LocalDateTime.now()
        );

        deviceWithId = new Device(
                1L,
                "Kitchen Thermostat",
                1L,
                56.9710,
                24.1604,
                "ON",
                LocalDateTime.now()
        );

        deviceDAOWithId = new DeviceDAO(
                1L,
                "Kitchen Thermostat",
                new DeviceTypeDAO(1L,"Thermostat"),
                56.9710,
                24.1604,
                "ON",
                LocalDateTime.now()
        );

        invalidDevice = new Device();
        token = "Bearer test-token";
    }

    @Test
    void testAddDevice_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.save(any(DeviceDAO.class))).thenReturn(deviceDAOWithId);

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
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

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).save(any(DeviceDAO.class));
    }

    @Test
    void testAddDevice_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceWithId)))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
    }

    @Test
    void testAddDevice_NoToken() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceWithId)))
                .andExpect(status().isForbidden());

        verify(authClient, times(0)).validateToken((anyString()));
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
    }

    @Test
    void testAddDevice_InvalidData() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDevice)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[*]", containsInAnyOrder(
                        "deviceName: Device name must not be blank",
                        "deviceType: Device type id must not be null",
                        "status: Device status must not be blank",
                        "latitude: Latitude values range between -90 and +90 degrees",
                        "longitude: Longitude values range between -180 and +180 degrees")));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
    }

    @Test
    void testAddDevice_DataIntegrityViolation() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.save(any(DeviceDAO.class))).thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceWithId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid data or foreign key constraint violation"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).save(any(DeviceDAO.class));
    }

    @Test
    void testUpdateDeviceById_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(validDeviceDAO));
        when(deviceRepository.save(any(DeviceDAO.class))).thenReturn(validDeviceDAO);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDevice)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceName", is("Smart Light")))
                .andExpect(jsonPath("$.deviceType", is(1)))
                .andExpect(jsonPath("$.latitude", is(56.9710)))
                .andExpect(jsonPath("$.longitude", is(24.1604)))
                .andExpect(jsonPath("$.status", is("OFF")));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findById(anyLong());
        verify(deviceRepository, times(1)).save(any(DeviceDAO.class));
    }

    @Test
    void testUpdateDeviceById_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDevice)))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(0)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
    }

    @Test
    void testUpdateDeviceById_NotFound() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDevice)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device not found with id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
    }

    @Test
    void testUpdateDeviceById_InvalidData() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDevice)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[*]", containsInAnyOrder(
                        "deviceName: Device name must not be blank",
                        "deviceType: Device type id must not be null",
                        "status: Device status must not be blank",
                        "latitude: Latitude values range between -90 and +90 degrees",
                        "longitude: Longitude values range between -180 and +180 degrees")));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(0)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
    }

    @Test
    void testUpdateDeviceById_UnexpectedError() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(validDeviceDAO));
        when(deviceRepository.save(any(DeviceDAO.class))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDevice)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("An unexpected error occurred. Please try again later."));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findById(anyLong());
        verify(deviceRepository, times(1)).save(any(DeviceDAO.class));
    }

    @Test
    void testDeleteDeviceById_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(validDeviceDAO));
        doNothing().when(deviceRepository).delete(any(DeviceDAO.class));

        mockMvc.perform(delete(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findById(anyLong());
        verify(deviceRepository, times(1)).delete(any(DeviceDAO.class));
    }

    @Test
    void testDeleteDeviceById_NoToken() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isForbidden());

        verify(authClient, times(0)).validateToken((anyString()));
        verify(deviceRepository, times(0)).findById(anyLong());
        verify(deviceRepository, times(0)).delete(any(DeviceDAO.class));
    }

    @Test
    void testDeleteDeviceById_NotFound() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(delete(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device not found with id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findById(anyLong());
        verify(deviceRepository, times(0)).delete(any(DeviceDAO.class));
    }

    @Test
    void testGetDeviceById_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(deviceDAOWithId));

        mockMvc.perform(get(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceId", is(1)))
                .andExpect(jsonPath("$.deviceName", is("Kitchen Thermostat")))
                .andExpect(jsonPath("$.deviceType", is(1)))
                .andExpect(jsonPath("$.latitude", is(56.9710)))
                .andExpect(jsonPath("$.longitude", is(24.1604)))
                .andExpect(jsonPath("$.status", is("ON")));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetDeviceById_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(0)).findById(anyLong());
    }

    @Test
    void testGetDeviceById_NotFound() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device not found with id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetDeviceById_InvalidId() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);

        mockMvc.perform(get(URL + "/{id}", "invalid")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid parameter type. Please provide a valid value."));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(0)).findById(anyLong());
    }

    @Test
    void testGetAllDevices_Success() throws Exception {
        List<DeviceDAO> deviceDAOList = Arrays.asList(deviceDAOWithId, validDeviceDAO);
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findAll()).thenReturn(deviceDAOList);

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceId").value(deviceWithId.getDeviceId()))
                .andExpect(jsonPath("$[0].deviceName").value(deviceWithId.getDeviceName()))
                .andExpect(jsonPath("$[0].latitude").value(deviceWithId.getLatitude()))
                .andExpect(jsonPath("$[1].deviceId").value(validDevice.getDeviceId()))
                .andExpect(jsonPath("$[1].status").value(validDevice.getStatus()))
                .andExpect(jsonPath("$[1].longitude").value(validDevice.getLongitude()));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findAll();
    }

    @Test
    void testGetAllDevices_NoToken() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isForbidden());

        verify(authClient, times(0)).validateToken((anyString()));
        verify(deviceRepository, times(0)).findAll();
    }

    @Test
    void testGetAllDevices_EmptyList() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findAll();
    }

}
