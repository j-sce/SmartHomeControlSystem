package com.smart.home.scenarioservice.client;

import com.smart.home.scenarioservice.model.dto.DeviceTypeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class DeviceTypeClient {

    @Value("${device.type.api.url}")
    private String deviceTypeApiUrl;

    private final WebClient webClient;


    public DeviceTypeDTO getDeviceTypeById(Long id, String bearerToken){
        String token = bearerToken.substring(7);
        return webClient.get()
                .uri(deviceTypeApiUrl + "/{id}", id)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(DeviceTypeDTO.class)
                .block();
    }

}
