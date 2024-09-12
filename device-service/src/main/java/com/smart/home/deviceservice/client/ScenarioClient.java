package com.smart.home.deviceservice.client;

import com.smart.home.deviceservice.model.dto.ScenarioDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Component
public class ScenarioClient {

    @Value("${scenario.api.url}")
    private String scenarioApiUrl;

    private final WebClient webClient;

    @Autowired
    public ScenarioClient(WebClient webClient) {
        this.webClient = webClient;
    }


    public List<ScenarioDTO> getScenariosByDeviceTypeId(Long deviceTypeId, String bearerToken) {
        String token = bearerToken.substring(7);

        URI uri = UriComponentsBuilder.fromUriString(scenarioApiUrl)
                .queryParam("deviceTypeId", deviceTypeId)
                .build()
                .toUri();

        log.debug("Requesting scenario data from: {}", uri.toString());

        return webClient.get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToFlux(ScenarioDTO.class)
                .collect(Collectors.toList())
                .block();
    }

}
