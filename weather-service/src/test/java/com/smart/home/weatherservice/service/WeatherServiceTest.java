package com.smart.home.weatherservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smart.home.weatherservice.client.WeatherDataClient;
import com.smart.home.weatherservice.handler.BadRequestException;
import com.smart.home.weatherservice.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherDataClient weatherDataClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private GeoOperations<String, Object> geoOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private WeatherService weatherService;

    private WeatherData weatherData;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(weatherService, "ttlMinutes", 1);
        ReflectionTestUtils.setField(weatherService, "cacheDistance", 1.0);

        weatherData = new WeatherData();
        weatherData.setLatitude(40.0);
        weatherData.setLongitude(50.0);
        weatherData.setWeatherIds(List.of(500));
        weatherData.setWeatherDescriptions(List.of("light rain"));
        weatherData.setTemperature(25.5);
        weatherData.setHumidity(80);
        weatherData.setWindSpeed(4.1);
        weatherData.setCloudiness(90);
        weatherData.setSunrise(LocalDateTime.of(2020, 11, 19, 12, 40));
        weatherData.setSunset(LocalDateTime.of(2020, 11, 19, 21, 0));
    }

    @Test
    void getWeatherData_CachedData_ReturnsCachedWeatherData() {
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.radius(anyString(), any(Circle.class))).thenReturn(
                new GeoResults<>(Collections.singletonList(new GeoResult<>(
                        new RedisGeoCommands.GeoLocation<>("weather:40.0:50.0", new Point(50.0, 40.0)),
                        new Distance(0.5)
                )))
        );
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("weather:40.0:50.0")).thenReturn(weatherData);

        WeatherData result = weatherService.getWeatherData(40.0, 50.0);

        assertNotNull(result);
        assertEquals(25.5, result.getTemperature());
        verify(redisTemplate, times(1)).opsForGeo();
        verify(redisTemplate, times(1)).opsForValue();
        verify(geoOperations, times(1)).radius(anyString(), any(Circle.class));
        verify(valueOperations, times(1)).get(anyString());
    }

    @Test
    void getWeatherData_NoCachedData_ReturnsWeatherDataFromExternalAPI() throws JsonProcessingException {
        double lat = 40.0;
        double lon = 50.0;
        String jsonString = "{\"coord\":{\"lon\":50.0,\"lat\":40.0},\"weather\":[{\"id\":500,\"description\":\"light rain\"}],\"main\":{\"temp\":25.5,\"humidity\":80},\"wind\":{\"speed\":4.1},\"clouds\":{\"all\":90},\"sys\":{\"sunrise\":1605782400,\"sunset\":1605812400}}";

        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.radius(anyString(), any(Circle.class))).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(weatherDataClient.getWeatherData(lat, lon)).thenReturn(jsonString);

        WeatherData result = weatherService.getWeatherData(lat, lon);

        assertNotNull(result);
        assertEquals(weatherData, result);
        verify(redisTemplate, times(2)).opsForGeo();
        verify(weatherDataClient, times(1)).getWeatherData(lat, lon);
    }

    @Test
    void getWeatherData_WeatherDataClientReturnsNull_ThrowsBadRequestException() {
        double lat = 51.51;
        double lon = -0.13;

        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.radius(anyString(), any(Circle.class))).thenReturn(null);
        when(weatherDataClient.getWeatherData(lat, lon)).thenReturn(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> weatherService.getWeatherData(lat, lon));

        assertEquals("No weather data found.", exception.getMessage());
        verify(weatherDataClient, times(1)).getWeatherData(lat, lon);
    }

    @Test
    void getWeatherData_InvalidJson_ThrowsBadRequestException() {
        double lat = 51.51;
        double lon = -0.13;
        String invalidJsonString = "{\"invalid_json\"}";

        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.radius(anyString(), any(Circle.class))).thenReturn(null);
        when(weatherDataClient.getWeatherData(lat, lon)).thenReturn(invalidJsonString);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> weatherService.getWeatherData(lat, lon));

        assertEquals("Error occurred during deserialization", exception.getMessage());
        verify(weatherDataClient, times(1)).getWeatherData(lat, lon);
    }

    @Test
    void parseWeatherData_Success() throws JsonProcessingException {
        String jsonString = "{\"coord\":{\"lon\":-0.13,\"lat\":51.51},\"weather\":[{\"id\":500,\"description\":\"light rain\"}],\"main\":{\"temp\":280.32,\"humidity\":81},\"wind\":{\"speed\":4.1},\"clouds\":{\"all\":90},\"sys\":{\"sunrise\":1605782400,\"sunset\":1605812400}}";

        WeatherData result = weatherService.parseWeatherData(jsonString);

        assertNotNull(result);
        assertEquals(51.51, result.getLatitude());
        assertEquals(-0.13, result.getLongitude());
        assertEquals(List.of(500), result.getWeatherIds());
        assertEquals(List.of("light rain"), result.getWeatherDescriptions());
        assertEquals(280.32, result.getTemperature());
        assertEquals(81, result.getHumidity());
        assertEquals(4.1, result.getWindSpeed());
        assertEquals(90, result.getCloudiness());
        assertEquals(LocalDateTime.of(2020, 11, 19, 12, 40), result.getSunrise());
        assertEquals(LocalDateTime.of(2020, 11, 19, 21, 0), result.getSunset());
    }

    @Test
    void parseWeatherData_InvalidJson_ThrowsBadRequestException() {
        String invalidJsonString = "{\"invalid_json\"}";

        BadRequestException exception = assertThrows(BadRequestException.class, () -> weatherService.parseWeatherData(invalidJsonString));

        assertEquals("Error occurred during deserialization", exception.getMessage());
    }

    @Test
    void parseWeatherData_UnexpectedError_ThrowsBadRequestException() {
        String invalidJsonString = null;
        BadRequestException exception = assertThrows(BadRequestException.class, () -> weatherService.parseWeatherData(invalidJsonString));

        assertEquals("Unexpected error while parsing weather data", exception.getMessage());
    }

    @Test
    void cacheWeatherData_Success() {
        double lat = 51.51;
        double lon = -0.13;

        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        weatherService.cacheWeatherData(lat, lon, weatherData);

        verify(valueOperations, times(1)).set(anyString(), any(WeatherData.class), eq(Duration.ofMinutes(1)));
        verify(geoOperations, times(1)).add(anyString(), any(Point.class), anyString());
    }

    @Test
    void getCachedWeatherData_NoData_ReturnsNull() {
        double lat = 51.51;
        double lon = -0.13;

        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.radius(anyString(), any(Circle.class))).thenReturn(null);

        WeatherData result = weatherService.getCachedWeatherData(lat, lon);

        assertNull(result);
    }

}
