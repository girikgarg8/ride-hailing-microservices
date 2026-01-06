package com.girikgarg.uberlocationservice.services.impl;

import com.girikgarg.uberlocationservice.dto.DriverLocationDto;
import com.girikgarg.uberlocationservice.services.api.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedisLocationServiceImpl implements LocationService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String DRIVER_GEO_OPS_KEY = "drivers";
    private static final Double SEARCH_RADIUS_KM = 5.0;

    public RedisLocationServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Boolean saveDriverLocation(String driverId, Double latitude, Double longitude) {
        try {
            GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();
            Long result = geoOps.add(
                    DRIVER_GEO_OPS_KEY,
                    new RedisGeoCommands.GeoLocation<>(
                            driverId,
                            new Point(longitude, latitude)
                    )
            );
            
            log.debug("Saved location for driver: {} at ({}, {})", driverId, latitude, longitude);
            return result != null && result > 0;
        } catch (Exception ex) {
            log.error("Error saving driver location for driverId: {}", driverId, ex);
            return false;
        }
    }

    @Override
    public List<DriverLocationDto> getNearbyDrivers(Double latitude, Double longitude) {
        try {
            GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();
            Distance radius = new Distance(SEARCH_RADIUS_KM, Metrics.KILOMETERS);
            Circle searchArea = new Circle(new Point(longitude, latitude), radius);

            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .includeCoordinates();

            GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOps.radius(
                    DRIVER_GEO_OPS_KEY,
                    searchArea,
                    args
            );

            if (results == null || results.getContent().isEmpty()) {
                log.debug("No drivers found near location ({}, {}) within {} km", 
                        latitude, longitude, SEARCH_RADIUS_KM);
                return Collections.emptyList();
            }

            List<DriverLocationDto> drivers = results.getContent().stream()
                    .map(GeoResult::getContent)
                    .filter(Objects::nonNull)
                    .map(geoLocation -> {
                        Point point = geoLocation.getPoint();
                        if (point == null) {
                            return null;
                        }
                        return DriverLocationDto.builder()
                                .driverId(geoLocation.getName())
                                .latitude(point.getY())
                                .longitude(point.getX())
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("Found {} drivers near location ({}, {}) within {} km", 
                    drivers.size(), latitude, longitude, SEARCH_RADIUS_KM);
            
            return drivers;
        } catch (Exception ex) {
            log.error("Error finding nearby drivers for location ({}, {})", latitude, longitude, ex);
            return Collections.emptyList();
        }
    }
}
