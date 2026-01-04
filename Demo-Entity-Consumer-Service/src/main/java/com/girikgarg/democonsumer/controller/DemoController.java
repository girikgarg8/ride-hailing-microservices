package com.girikgarg.democonsumer.controller;

import com.girikgarg.democonsumer.repository.BookingRepository;
import com.girikgarg.democonsumer.repository.DriverRepository;
import com.girikgarg.democonsumer.repository.PassengerRepository;
import com.girikgarg.uberentityservice.models.Booking;
import com.girikgarg.uberentityservice.models.Driver;
import com.girikgarg.uberentityservice.models.Passenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Demo service is working! ✅");
        response.put("entityServiceImported", "true");
        response.put("info", "Successfully imported entities from uber-entity-service");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/drivers")
    public ResponseEntity<Map<String, Object>> getAllDrivers() {
        List<Driver> drivers = driverRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("count", drivers.size());
        response.put("drivers", drivers);
        response.put("message", "Successfully fetched drivers using imported Driver entity! ✅");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/passengers")
    public ResponseEntity<Map<String, Object>> getAllPassengers() {
        List<Passenger> passengers = passengerRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("count", passengers.size());
        response.put("passengers", passengers);
        response.put("message", "Successfully fetched passengers using imported Passenger entity! ✅");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("count", bookings.size());
        response.put("bookings", bookings);
        response.put("message", "Successfully fetched bookings using imported Booking entity! ✅");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/driver")
    public ResponseEntity<Map<String, Object>> createDriver(@RequestBody Map<String, String> request) {
        // ⭐ Creating a Driver using the imported entity
        Driver driver = Driver.builder()
                .name(request.get("name"))
                .licenseNumber(request.get("licenseNumber"))
                .phoneNumber(request.get("phoneNumber"))
                .build();
        
        Driver saved = driverRepository.save(driver);
        
        Map<String, Object> response = new HashMap<>();
        response.put("driver", saved);
        response.put("message", "Successfully created driver using imported Driver entity! ✅");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/passenger")
    public ResponseEntity<Map<String, Object>> createPassenger(@RequestBody Map<String, String> request) {
        // ⭐ Creating a Passenger using the imported entity
        Passenger passenger = Passenger.builder()
                .name(request.get("name"))
                .email(request.get("email"))
                .phoneNumber(request.get("phoneNumber"))
                .password(request.get("password"))
                .build();
        
        Passenger saved = passengerRepository.save(passenger);
        
        Map<String, Object> response = new HashMap<>();
        response.put("passenger", saved);
        response.put("message", "Successfully created passenger using imported Passenger entity! ✅");
        return ResponseEntity.ok(response);
    }
}

