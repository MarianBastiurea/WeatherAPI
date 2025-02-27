package com.marianbastiurea.weatherapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherService weatherService;

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/{date}")
    public Weather getWeatherForDate(@PathVariable("date") String date) {
        LocalDate localDate = LocalDate.parse(date);
        System.out.println("Fetching weather data for date: " + date);
        Weather weather = weatherService.getWeatherByDate(localDate);
        System.out.println("Weather data fetched: " + weather);
        return weather;
    }

    @GetMapping
    public List<Weather> getWeatherByDateRange(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {
        return weatherService.getWeatherByDateRange(startDate, endDate);
    }

}
