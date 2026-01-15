package com.girikgarg.uberauthservice.services.impl;

import com.girikgarg.uberauthservice.helpers.AuthPassengerDetails;
import com.girikgarg.uberauthservice.repositories.PassengerRepository;
import com.girikgarg.uberentityservice.models.Passenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// This class is responsible for loading the user in form of UserDetails object for auth
@Service
public class UserServiceImpl implements UserDetailsService {
    
    @Autowired
    private PassengerRepository passengerRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Passenger passenger = passengerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cannot find passenger by given email: " + email));

        return new AuthPassengerDetails(passenger);
    }
}
