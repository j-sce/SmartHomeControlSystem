package com.smart.home.weatherservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smart.home.weatherservice.client.WeatherDataClient;
import com.smart.home.weatherservice.handler.BadRequestException;
import com.smart.home.weatherservice.model.WeatherData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherDataClient weatherDataClient;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void getWeatherData_Success() throws JsonProcessingException {
        double lat = 51.51;
        double lon = -0.13;
        String jsonString = "{\"coord\":{\"lon\":-0.13,\"lat\":51.51},\"weather\":[{\"id\":500,\"description\":\"light rain\"}],\"main\":{\"temp\":280.32,\"humidity\":81},\"wind\":{\"speed\":4.1},\"clouds\":{\"all\":90},\"sys\":{\"sunrise\":1605782400,\"sunset\":1605812400}}";
        WeatherData expectedWeatherData = new WeatherData();
        expectedWeatherData.setLatitude(lat);
        expectedWeatherData.setLongitude(lon);
        expectedWeatherData.setWeatherIds(List.of(500));
        expectedWeatherData.setWeatherDescriptions(List.of("light rain"));
        expectedWeatherData.setTemperature(280.32);
        expectedWeatherData.setHumidity(81);
        expectedWeatherData.setWindSpeed(4.1);
        expectedWeatherData.setCloudiness(90);
        expectedWeatherData.setSunrise(LocalDateTime.of(2020, 11, 19, 12, 40));
        expectedWeatherData.setSunset(LocalDateTime.of(2020, 11, 19, 21, 0));

        when(weatherDataClient.getWeatherData(lat, lon)).thenReturn(jsonString);

        WeatherData result = weatherService.getWeatherData(lat, lon);

        assertNotNull(result);
        assertEquals(expectedWeatherData, result);
        verify(weatherDataClient, times(1)).getWeatherData(lat, lon);
    }

    @Test
    void getWeatherData_WeatherDataClientReturnsNull_ThrowsBadRequestException() {
        double lat = 51.51;
        double lon = -0.13;

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

        when(weatherDataClient.getWeatherData(lat, lon)).thenReturn(invalidJsonString);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> weatherService.getWeatherData(lat, lon));

        assertEquals("Error occurred during deserialization", exception.getMessage());
        verify(weatherDataClient, times(1)).getWeatherData(lat, lon);
    }

    @Test
    void getWeatherData_UnexpectedError_ThrowsBadRequestException() {
        double lat = 51.51;
        double lon = -0.13;
        String jsonString = "{\"coord\":{\"lon\":-0.13,\"lat\":51.51},\"weather\":[{\"id\":500,\"description\":\"light rain\"}],\"main\":{\"temp\":280.32,\"humidity\":81},\"wind\":{\"speed\":4.1},\"clouds\":{\"all\":90},\"sys\":{\"sunrise\":1605782400,\"sunset\":1605812400}}";
        when(weatherDataClient.getWeatherData(lat, lon)).thenReturn(jsonString);
        when(weatherService.getWeatherData(lat, lon)).thenThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> weatherService.getWeatherData(lat, lon));

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

}
