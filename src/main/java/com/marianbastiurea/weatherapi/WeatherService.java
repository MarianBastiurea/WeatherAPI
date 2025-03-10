package com.marianbastiurea.weatherapi;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private List<Weather> weatherList = new ArrayList<>();

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


            CSVParser parser = CSVFormat.DEFAULT
                    .withHeader("date", "temperature", "tmin", "tmax", "precipitation", "snow",
                            "wdir", "windSpeed", "wpgt", "pres", "tsun")
                    .withFirstRecordAsHeader()
                    .parse(reader);


            Map<String, Integer> headers = parser.getHeaderMap();
            System.out.println("Headers found: " + headers.keySet());

            for (CSVRecord record : parser) {
                Weather weather = new Weather();
                weather.setDate(record.get("date"));
                weather.setTemperature(Double.parseDouble(record.get("temperature")));
                String precipitationValue = record.get("precipitation");
                if (precipitationValue != null && !precipitationValue.trim().isEmpty()) {
                    weather.setPrecipitation(Double.parseDouble(precipitationValue));
                } else {
                    weather.setPrecipitation(0.0);
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
                    MonthDay weatherMonthDay = MonthDay.from(weatherDate);
                    MonthDay startMonthDay = MonthDay.from(startDate);
                    MonthDay endMonthDay = MonthDay.from(endDate);

                    return (weatherMonthDay.equals(startMonthDay) || weatherMonthDay.isAfter(startMonthDay)) &&
                            (weatherMonthDay.equals(endMonthDay) || weatherMonthDay.isBefore(endMonthDay));
                })
                .collect(Collectors.toList());
    }

    public Weather getWeatherByDate(LocalDate date) {
        MonthDay searchMonthDay = MonthDay.from(date);
        return weatherList.stream()
                .filter(weather -> {
                    LocalDate weatherDate = LocalDate.parse(weather.getDate());
                    MonthDay weatherMonthDay = MonthDay.from(weatherDate);
                    return weatherMonthDay.equals(searchMonthDay);
                })
                .findFirst()
                .orElse(null);
    }

}
