package com.girikgarg.uberlocationservice.services.api;

import com.girikgarg.uberlocationservice.dto.DriverLocationDto;

import java.util.List;

public interface LocationService {
    Boolean saveDriverLocation(String driverId, Double latitude, Double longitude);

    List<DriverLocationDto> getNearbyDrivers(Double latitude, Double longitude);
}
