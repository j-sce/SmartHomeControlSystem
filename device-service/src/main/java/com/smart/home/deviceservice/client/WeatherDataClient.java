package com.smart.home.deviceservice.client;

import com.smart.home.deviceservice.model.dto.WeatherDataDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Log4j2
@Component
public class WeatherDataClient {

    @Value("${weather.data.api.url}")
    private String weatherDataApiUrl;

    private final WebClient webClient;

    @Autowired
    public WeatherDataClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public WeatherDataDTO getWeatherData(double lat, double lon, String bearerToken) {
        String token = bearerToken.substring(7);

        URI uri = UriComponentsBuilder.fromUriString(weatherDataApiUrl)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .build()
                .toUri();

        log.debug("Requesting weather data from: {}", uri.toString());

        return webClient.get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(WeatherDataDTO.class)
                .block();
    }

}
