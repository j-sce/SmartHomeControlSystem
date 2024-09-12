package com.smart.home.deviceservice.intergation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.deviceservice.client.AuthClient;
import com.smart.home.deviceservice.repository.DeviceRepository;
import com.smart.home.deviceservice.repository.DeviceStatusChangeRepository;
import com.smart.home.deviceservice.repository.model.DeviceDAO;
import com.smart.home.deviceservice.repository.model.DeviceStatusChangeDAO;
import com.smart.home.deviceservice.repository.model.DeviceTypeDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class DeviceStatusChangeServiceEndToEndTest {

    private static final String URL = "/api/device/status";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceStatusChangeRepository deviceStatusChangeRepository;

    @MockBean
    private DeviceRepository deviceRepository;

    @MockBean
    private AuthClient authClient;

    @Autowired
    private ObjectMapper objectMapper;

    private DeviceStatusChangeDAO deviceStatusChangeDAO;
    private DeviceDAO deviceDAO;
    private String token;

    @BeforeEach
    void setUp() {

        deviceStatusChangeDAO = new DeviceStatusChangeDAO();
        deviceStatusChangeDAO.setId(1L);
        deviceStatusChangeDAO.setOldStatus("OFF");
        deviceStatusChangeDAO.setDevice(new DeviceDAO(1L));
        deviceStatusChangeDAO.setNewStatus("ON");
        deviceStatusChangeDAO.setWeatherCondition("manual status change");

        deviceDAO = new DeviceDAO(
                1L,
                "Smart Light",
                new DeviceTypeDAO(1L, "Light"),
                56.9710,
                24.1604,
                "OFF",
                LocalDateTime.now()
        );

        token = "Bearer test-token";
    }

    @Test
    void testChangeDeviceStatus_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(deviceDAO));
        when(deviceRepository.save(any(DeviceDAO.class))).thenReturn(deviceDAO);
        when(deviceStatusChangeRepository.save(any(DeviceStatusChangeDAO.class))).thenReturn(deviceStatusChangeDAO);

        mockMvc.perform(patch(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .param("status", "ON")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.device", is(1)))
                .andExpect(jsonPath("$.oldStatus", is("OFF")))
                .andExpect(jsonPath("$.newStatus", is("ON")))
                .andExpect(jsonPath("$.weatherCondition", is("manual status change")));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(2)).findById(anyLong());
        verify(deviceRepository, times(1)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(1)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testChangeDeviceStatus_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(patch(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .param("status", "ON")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(0)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(0)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testChangeDeviceStatus_NoToken() throws Exception {
        mockMvc.perform(patch(URL + "/{id}", 1L)
                        .param("status", "ON")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(authClient, times(0)).validateToken((anyString()));
        verify(deviceRepository, times(0)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(0)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testChangeDeviceStatus_DeviceNotFound() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(patch(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .param("status", "ON")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device not found with id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(0)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testChangeDeviceStatus_SameStatus() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(deviceDAO));

        mockMvc.perform(patch(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .param("status", "OFF")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device already has the status 'OFF'. No change necessary."));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(1)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(0)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testChangeDeviceStatus_UnexpectedException() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(deviceDAO));
        when(deviceRepository.save(any(DeviceDAO.class))).thenReturn(deviceDAO);
        when(deviceStatusChangeRepository.save(any(DeviceStatusChangeDAO.class))).thenThrow(new RuntimeException("Unexpected error occurred."));

        mockMvc.perform(patch(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .param("status", "ON")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceRepository, times(2)).findById(anyLong());
        verify(deviceRepository, times(1)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(1)).save(any(DeviceStatusChangeDAO.class));
    }

}
