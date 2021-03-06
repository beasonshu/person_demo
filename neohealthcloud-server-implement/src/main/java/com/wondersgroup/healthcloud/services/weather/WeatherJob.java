package com.wondersgroup.healthcloud.services.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wondersgroup.healthcloud.jpa.entity.weather.WeatherArea;
import com.wondersgroup.healthcloud.jpa.repository.weather.WeatherAreaRepository;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * ░░░░░▄█▌▀▄▓▓▄▄▄▄▀▀▀▄▓▓▓▓▓▌█
 * ░░░▄█▀▀▄▓█▓▓▓▓▓▓▓▓▓▓▓▓▀░▓▌█
 * ░░█▀▄▓▓▓███▓▓▓███▓▓▓▄░░▄▓▐█▌
 * ░█▌▓▓▓▀▀▓▓▓▓███▓▓▓▓▓▓▓▄▀▓▓▐█
 * ▐█▐██▐░▄▓▓▓▓▓▀▄░▀▓▓▓▓▓▓▓▓▓▌█▌
 * █▌███▓▓▓▓▓▓▓▓▐░░▄▓▓███▓▓▓▄▀▐█
 * █▐█▓▀░░▀▓▓▓▓▓▓▓▓▓██████▓▓▓▓▐█
 * ▌▓▄▌▀░▀░▐▀█▄▓▓██████████▓▓▓▌█▌
 * ▌▓▓▓▄▄▀▀▓▓▓▀▓▓▓▓▓▓▓▓█▓█▓█▓▓▌█▌
 * █▐▓▓▓▓▓▓▄▄▄▓▓▓▓▓▓█▓█▓█▓█▓▓▓▐█
 * <p>
 * Created by zhangzhixiu on 02/12/2016.
 */
@Component
public class WeatherJob {

    private static final Logger logger = LoggerFactory.getLogger(WeatherJob.class);
    private static final String[] weekday = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    @Autowired
    private HeWeatherClient heWeatherClient;

    @Autowired
    private YahooWeatherClient yahooWeatherClient;

    @Autowired
    private WeatherCache weatherCache;

    @Autowired
    private YahooWeatherCodeCacheUtil codeCache;

    @Autowired
    private WeatherAreaRepository weatherAreaRepository;

    @Autowired
    private WeatherHintUtil weatherHintUtil;

    public void hourlyJob() {
        String updateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(new DateTime());
        JsonNode heShanghai = heWeatherClient.weather(null).get("HeWeather5").get(0);
        DateTimeFormatter sdf = DateTimeFormat.forPattern("dd MMM yyyy").withLocale(Locale.ENGLISH);
        DateTimeFormatter newdf = DateTimeFormat.forPattern("MM/dd");

        List<WeatherArea> tasks = tasks();
        for (WeatherArea task : tasks) {
            try {
                JsonNode he = heShanghai;
                if (task.getHecode() != null) {
                    he = heWeatherClient.weather(task.getHecode()).get("HeWeather5").get(0);
                }

                JsonNode channel = yahooWeatherClient.channel(task.getWoeid());

                ObjectNode cache = JsonNodeFactory.instance.objectNode();
                ObjectNode brief = JsonNodeFactory.instance.objectNode();

                cache.put("update_time", updateTime);
                brief.put("update_time", updateTime);
                cache.put("name", task.getName());
                brief.put("name", task.getName());
                cache.put("code", task.getCode());
                brief.put("code", task.getCode());


                ObjectNode today = JsonNodeFactory.instance.objectNode();
                today.put("weather_code", channel.get("item").get("condition").get("code").asText());
                brief.put("weather_code", channel.get("item").get("condition").get("code").asText());
                today.put("weather_name", codeCache.get(today.get("weather_code").asText()).getName());
                brief.put("weather_name", codeCache.get(brief.get("weather_code").asText()).getName());
                today.put("current_temperature", channel.get("item").get("condition").get("temp").asText());
                brief.put("current_temperature", channel.get("item").get("condition").get("temp").asText());
                today.put("wind_direction", he.get("now").get("wind").get("dir").asText());
                today.put("wind_level", he.get("now").get("wind").get("sc").asText());
                cache.set("today", today);

                cache.set("astronomy", channel.get("astronomy"));

                ObjectNode aqi = JsonNodeFactory.instance.objectNode();
                aqi.put("aqi", he.get("aqi").get("city").get("aqi").asText());
                brief.put("aqi", he.get("aqi").get("city").get("qlty").asText());
                aqi.put("quality", he.get("aqi").get("city").get("qlty").asText());
                cache.set("aqi", aqi);

                cache.set("suggestion", he.get("suggestion"));

                ArrayNode forecasts = JsonNodeFactory.instance.arrayNode(10);
                ArrayNode yahooForecasts = (ArrayNode) channel.get("item").get("forecast");
                today.put("low", yahooForecasts.get(0).get("low").asText());
                today.put("high", yahooForecasts.get(0).get("high").asText());
                for (int i = 1; i < 6; i++) {
                    JsonNode yahooForecast = yahooForecasts.get(i);
                    ObjectNode forecast = JsonNodeFactory.instance.objectNode();
                    forecast.put("weather_code", yahooForecast.get("code").asText());
                    forecast.put("weather_name", codeCache.get(yahooForecast.get("code").asText()).getName());
                    forecast.put("high", yahooForecast.get("high").asText());
                    forecast.put("low", yahooForecast.get("low").asText());
                    DateTime date = sdf.parseDateTime(yahooForecast.get("date").asText());
                    forecast.put("date", newdf.print(date));
                    forecast.put("weekday", weekday[date.getDayOfWeek() - 1]);
                    forecasts.add(forecast);
                }
                cache.set("forecast", forecasts);

                String[] hint = weatherHintUtil.get(Integer.valueOf(he.get("aqi").get("city").get("aqi").asText()), Integer.valueOf(channel.get("item").get("condition").get("code").asText()), Integer.valueOf(channel.get("item").get("condition").get("temp").asText()));

                brief.put("hint", hint[0]);
                cache.put("hint", hint[1]);

                saveToRedis(WeatherCache.Type.ALL, task.getCode(), cache.toString());
                saveToRedis(WeatherCache.Type.BRIEF, task.getCode(), brief.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                continue;
            }
        }
    }

    private void saveToRedis(WeatherCache.Type type, String code, String value) {
        logger.info(value);
        weatherCache.save(type, code, value);
    }

    public List<WeatherArea> tasks() {
        return weatherAreaRepository.findActive();
    }
}
