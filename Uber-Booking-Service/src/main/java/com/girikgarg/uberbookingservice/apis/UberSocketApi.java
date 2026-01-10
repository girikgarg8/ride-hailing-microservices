package com.girikgarg.uberbookingservice.apis;

import com.girikgarg.uberbookingservice.dto.RideRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.Map;

/**
 * Retrofit API interface for communicating with Uber Socket Service.
 * Service is discovered via Eureka with service name: UBER-SOCKET-SERVICE
 */
public interface UberSocketApi {

    /**
     * Sends a new ride request to the Socket Service to be broadcast to connected drivers.
     * 
     * @param requestDto The ride request details
     * @return Call<Map<String, String>> Response from the Socket Service with message and bookingId
     */
    @POST("api/socket/newride")
    Call<Map<String, String>> raiseRideRequest(@Body RideRequestDto requestDto);
}

