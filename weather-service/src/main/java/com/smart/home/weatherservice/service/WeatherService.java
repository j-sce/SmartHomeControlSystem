package com.smart.home.weatherservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.weatherservice.client.WeatherDataClient;
import com.smart.home.weatherservice.handler.BadRequestException;
import com.smart.home.weatherservice.model.WeatherData;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherDataClient weatherDataClient;


    @Cacheable(value = "weatherData", key = "#lat + ',' + #lon")
    public WeatherData getWeatherData(double lat, double lon) {
        String jsonString = getWeather(lat, lon);
        log.info("Weather data: {}", jsonString);

        return parseWeatherData(jsonString);
    }


    String getWeather(double lat, double lon) {
        log.debug("Getting weather data from OpenWeatherMap API");
        String weatherData = weatherDataClient.getWeatherData(lat, lon);

        if (weatherData == null) {
            log.error("Failed to retrieve weather data for lat: {} and lon: {}", lat, lon);
            throw new BadRequestException("No weather data found.");
        }

        return weatherData;
    }

    WeatherData parseWeatherData(String jsonString) {
        log.debug("Parsing weather data to WeatherData Object");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);

            WeatherData weatherData = new WeatherData();

            // Extract latitude and longitude
            JsonNode coordNode = rootNode.path("coord");
            weatherData.setLatitude(coordNode.path("lat").asDouble());
            weatherData.setLongitude(coordNode.path("lon").asDouble());

            // Extract weather details
            List<Integer> weatherIds = new ArrayList<>();
            List<String> weatherDescriptions = new ArrayList<>();
            JsonNode weatherNode = rootNode.path("weather");
            for (JsonNode weather : weatherNode) {
                weatherIds.add(weather.path("id").asInt());
                weatherDescriptions.add(weather.path("description").asText());
            }
            weatherData.setWeatherIds(weatherIds);
            weatherData.setWeatherDescriptions(weatherDescriptions);

            // Extract temperature, humidity, wind speed, and cloudiness
            JsonNode mainNode = rootNode.path("main");
            weatherData.setTemperature(mainNode.path("temp").asDouble());
            weatherData.setHumidity(mainNode.path("humidity").asInt());

            JsonNode windNode = rootNode.path("wind");
            weatherData.setWindSpeed(windNode.path("speed").asDouble());

            JsonNode cloudsNode = rootNode.path("clouds");
            weatherData.setCloudiness(cloudsNode.path("all").asInt());

            // Extract sunrise and sunset
            JsonNode sysNode = rootNode.path("sys");
            weatherData.setSunrise(convertTimestampToLocalDateTime(sysNode.path("sunrise").asLong()));
            weatherData.setSunset(convertTimestampToLocalDateTime(sysNode.path("sunset").asLong()));

            return weatherData;
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException while parsing weather data: {}", e.getMessage());
            throw new BadRequestException("Error occurred during deserialization");
        } catch (Exception e){
            log.error("Unexpected error while parsing weather data: {}", e.getMessage());
            throw new BadRequestException("Unexpected error while parsing weather data");
        }
    }

    private static LocalDateTime convertTimestampToLocalDateTime(long timestamp) {
        return Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

}
