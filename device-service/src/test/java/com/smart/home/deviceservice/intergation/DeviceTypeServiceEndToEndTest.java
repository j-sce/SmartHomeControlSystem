package com.smart.home.deviceservice.intergation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.deviceservice.client.AuthClient;
import com.smart.home.deviceservice.model.DeviceType;
import com.smart.home.deviceservice.repository.DeviceTypeRepository;
import com.smart.home.deviceservice.repository.model.DeviceTypeDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
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
@AutoConfigureMockMvc
public class DeviceTypeServiceEndToEndTest {

    private static final String URL = "/api/device/type";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceTypeRepository deviceTypeRepository;

    @MockBean
    private AuthClient authClient;

    @Autowired
    private ObjectMapper objectMapper;

    private DeviceType validDeviceType;
    private DeviceTypeDAO validDeviceTypeDAO;
    private DeviceType deviceTypeWithId;
    private DeviceTypeDAO deviceTypeDAOWithId;
    private DeviceType invalidDeviceType;
    private String token;

    @BeforeEach
    void setUp() {
        validDeviceType = new DeviceType();
        validDeviceType.setDeviceTypeName("Smart Light");

        validDeviceTypeDAO = new DeviceTypeDAO("Smart Light");

        deviceTypeWithId = new DeviceType();
        deviceTypeWithId.setDeviceTypeId(1L);
        deviceTypeWithId.setDeviceTypeName("Thermostat");

        deviceTypeDAOWithId = new DeviceTypeDAO(1L, "Thermostat");

        invalidDeviceType = new DeviceType();
        token = "Bearer test-token";
    }

    @Test
    void testAddDeviceType_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.save(any(DeviceTypeDAO.class))).thenReturn(deviceTypeDAOWithId);

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("deviceType", "Thermostat"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceTypeId").value(deviceTypeWithId.getDeviceTypeId()))
                .andExpect(jsonPath("$.deviceTypeName").value(deviceTypeWithId.getDeviceTypeName()));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).save(any(DeviceTypeDAO.class));
    }

    @Test
    void testAddDeviceType_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("deviceType", "Thermostat"))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(0)).save(any(DeviceTypeDAO.class));
    }

    @Test
    void testAddDeviceType_NoToken() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("deviceType", "Thermostat"))
                .andExpect(status().isForbidden());

        verify(authClient, times(0)).validateToken((anyString()));
        verify(deviceTypeRepository, times(0)).save(any(DeviceTypeDAO.class));
    }

    @Test
    void testAddDeviceType_DataIntegrityViolation() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.save(any(DeviceTypeDAO.class))).thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("deviceType", "Thermostat"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid data or foreign key constraint violation"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).save(any(DeviceTypeDAO.class));
    }

    @Test
    void testUpdateDeviceTypeById_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.findById(anyLong())).thenReturn(Optional.of(validDeviceTypeDAO));
        when(deviceTypeRepository.save(any(DeviceTypeDAO.class))).thenReturn(validDeviceTypeDAO);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("deviceType", "Smart Light"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceTypeName").value(validDeviceType.getDeviceTypeName()));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).findById(anyLong());
        verify(deviceTypeRepository, times(1)).save(any(DeviceTypeDAO.class));
    }

    @Test
    void testUpdateDeviceTypeById_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("deviceType", "Thermostat"))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(0)).findById(anyLong());
        verify(deviceTypeRepository, times(0)).save(any(DeviceTypeDAO.class));
    }

    @Test
    void testUpdateDeviceTypeById_NotFound() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("deviceType", "Smart Light"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device type not found with id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).findById(anyLong());
        verify(deviceTypeRepository, times(0)).save(any(DeviceTypeDAO.class));
    }

    @Test
    void testUpdateDeviceTypeById_UnexpectedError() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.findById(anyLong())).thenReturn(Optional.of(validDeviceTypeDAO));
        when(deviceTypeRepository.save(any(DeviceTypeDAO.class))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("deviceType", "Thermostat"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("An unexpected error occurred. Please try again later."));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).findById(anyLong());
        verify(deviceTypeRepository, times(1)).save(any(DeviceTypeDAO.class));
    }

    @Test
    void testDeleteDeviceTypeById_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.findById(anyLong())).thenReturn(Optional.of(validDeviceTypeDAO));
        doNothing().when(deviceTypeRepository).delete(any(DeviceTypeDAO.class));

        mockMvc.perform(delete(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).findById(anyLong());
        verify(deviceTypeRepository, times(1)).delete(any(DeviceTypeDAO.class));
    }

    @Test
    void testDeleteDeviceTypeById_NoToken() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isForbidden());

        verify(authClient, times(0)).validateToken((anyString()));
        verify(deviceTypeRepository, times(0)).findById(anyLong());
        verify(deviceTypeRepository, times(0)).delete(any(DeviceTypeDAO.class));
    }

    @Test
    void testDeleteDeviceTypeById_NotFound() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(delete(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device type not found with id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).findById(anyLong());
        verify(deviceTypeRepository, times(0)).delete(any(DeviceTypeDAO.class));
    }

    @Test
    void testGetDeviceTypeById_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.findById(anyLong())).thenReturn(Optional.of(deviceTypeDAOWithId));

        mockMvc.perform(get(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceTypeId").value(1L))
                .andExpect(jsonPath("$.deviceTypeName").value("Thermostat"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetDeviceTypeById_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(0)).findById(anyLong());
    }

    @Test
    void testGetDeviceTypeById_NotFound() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device type not found with id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetDeviceTypeById_InvalidId() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);

        mockMvc.perform(get(URL + "/{id}", "invalid")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid parameter type. Please provide a valid value."));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(0)).findById(anyLong());
    }

    @Test
    void testGetAllDeviceTypes_Success() throws Exception {
        List<DeviceTypeDAO> deviceTypeDAOList = Arrays.asList(deviceTypeDAOWithId, validDeviceTypeDAO);
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.findAll()).thenReturn(deviceTypeDAOList);

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceTypeId").value(deviceTypeWithId.getDeviceTypeId()))
                .andExpect(jsonPath("$[0].deviceTypeName").value(deviceTypeWithId.getDeviceTypeName()))
                .andExpect(jsonPath("$[1].deviceTypeName").value(validDeviceType.getDeviceTypeName()));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).findAll();
    }

    @Test
    void testGetAllDeviceTypes_NoToken() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isForbidden());

        verify(authClient, times(0)).validateToken((anyString()));
        verify(deviceTypeRepository, times(0)).findAll();
    }

    @Test
    void testGetAllDeviceTypes_EmptyList() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeRepository, times(1)).findAll();
    }
}
