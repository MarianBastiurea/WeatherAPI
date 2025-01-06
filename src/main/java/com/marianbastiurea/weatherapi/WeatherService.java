package com.marianbastiurea.weatherapi;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeatherService {
    private List<Weather> weatherList = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    public WeatherService() {
        try {
            logger.info("Starting to load weather data...");
            loadWeatherData("WeatherDataForLondon.csv");
            logger.info("Weather data loaded successfully. Total records: {}", weatherList.size());
        } catch (Exception e) {
            logger.error("Failed to load weather data: {}", e.getMessage(), e);
            throw new RuntimeException("Error initializing WeatherService", e);
        }
    }

    public List<Weather> loadWeatherData(String filePath) {
        try (Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filePath))) {
            if (reader == null) {
                throw new FileNotFoundException("File " + filePath + " not found in resources");
            }

            // Creează parserul CSV
            CSVParser parser = CSVFormat.DEFAULT
                    .withHeader("date", "temperature", "tmin", "tmax", "precipitation", "snow",
                            "wdir", "windSpeed", "wpgt", "pres", "tsun")
                    .withFirstRecordAsHeader()
                    .parse(reader);

            // Verifică header-ele disponibile (opțional)
            Map<String, Integer> headers = parser.getHeaderMap();
            System.out.println("Headers found: " + headers.keySet());

            // Parcurge rândurile
            for (CSVRecord record : parser) {
                Weather weather = new Weather();
                weather.setDate(record.get("date")); // Asigură-te că numele headerului este corect
                weather.setTemperature(Double.parseDouble(record.get("temperature")));
                String precipitationValue = record.get("precipitation");
                if (precipitationValue != null && !precipitationValue.trim().isEmpty()) {
                    weather.setPrecipitation(Double.parseDouble(precipitationValue));
                } else {
                    weather.setPrecipitation(0.0); // sau o valoare implicită dacă este cazul
                }

                weather.setWindSpeed(Double.parseDouble(record.get("windSpeed")));
                weatherList.add(weather);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file", e);
        }
        return weatherList;
    }


    public List<Weather> getWeatherByDateRange(LocalDate startDate, LocalDate endDate) {
        System.out.println("Received startDate in WeatherAPIService: " + startDate + ", endDate: " + endDate);
        return weatherList.stream()
                .filter(weather -> {
                    LocalDate weatherDate = LocalDate.parse(weather.getDate());
                    return (weatherDate.isEqual(startDate) || weatherDate.isAfter(startDate)) &&
                            (weatherDate.isEqual(endDate) || weatherDate.isBefore(endDate));
                })
                .collect(Collectors.toList());
    }

    public Weather getWeatherByDate(LocalDate date) {
        return weatherList.stream()
                .filter(weather -> LocalDate.parse(weather.getDate()).isEqual(date))
                .findFirst()
                .orElse(null);
    }

    public List<Weather> getWeatherForInterval(LocalDate start, LocalDate end) {
        return weatherList.stream()
                .filter(weather -> {
                    LocalDate weatherDate = LocalDate.parse(weather.getDate());
                    return !weatherDate.isBefore(start) && !weatherDate.isAfter(end);
                })
                .collect(Collectors.toList());
    }

}
