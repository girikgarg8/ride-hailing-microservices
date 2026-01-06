package com.girikgarg.uberlocationservice.controller;

import com.girikgarg.uberlocationservice.dto.DriverLocationDto;
import com.girikgarg.uberlocationservice.dto.NearbyDriversRequestDto;
import com.girikgarg.uberlocationservice.dto.SaveDriverLocationRequestDto;
import com.girikgarg.uberlocationservice.services.api.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/drivers")
    public ResponseEntity<Boolean> saveDriverLocation(@RequestBody SaveDriverLocationRequestDto saveDriverLocationRequestDto) {
        try {
            Boolean response = locationService.saveDriverLocation(
                    saveDriverLocationRequestDto.getDriverId(),
                    saveDriverLocationRequestDto.getLatitude(),
                    saveDriverLocationRequestDto.getLongitude()
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/nearby/drivers")
    public ResponseEntity<List<DriverLocationDto>> getNearbyDrivers(@RequestBody NearbyDriversRequestDto nearbyDriversRequestDto) {
        try {
            List<DriverLocationDto> drivers = locationService.getNearbyDrivers(
                    nearbyDriversRequestDto.getLatitude(),
                    nearbyDriversRequestDto.getLongitude()
            );
            return new ResponseEntity<>(drivers, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
