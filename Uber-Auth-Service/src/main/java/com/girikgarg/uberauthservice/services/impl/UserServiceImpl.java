package com.girikgarg.uberauthservice.services.impl;

import com.girikgarg.uberauthservice.helpers.AuthUserDetails;
import com.girikgarg.uberauthservice.repositories.DriverRepository;
import com.girikgarg.uberauthservice.repositories.PassengerRepository;
import com.girikgarg.uberentityservice.models.Driver;
import com.girikgarg.uberentityservice.models.Passenger;
import com.girikgarg.uberentityservice.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// This class is responsible for loading the user in form of UserDetails object for auth
@Service
public class UserServiceImpl implements UserDetailsService {
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private PassengerRepository passengerRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try to find driver first
        var driverOpt = driverRepository.findByEmail(email);
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            return new AuthUserDetails(driver.getId(), driver.getEmail(), driver.getPassword(), Role.DRIVER);
        }
        
        // Try to find passenger
        var passengerOpt = passengerRepository.findByEmail(email);
        if (passengerOpt.isPresent()) {
            Passenger passenger = passengerOpt.get();
            return new AuthUserDetails(passenger.getId(), passenger.getEmail(), passenger.getPassword(), Role.PASSENGER);
        }
        
        throw new UsernameNotFoundException("Cannot find user by given email: " + email);
    }
}
