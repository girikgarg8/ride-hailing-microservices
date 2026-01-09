package com.girikgarg.uberbookingservice.apis;

import com.girikgarg.uberbookingservice.dto.RideRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit API interface for communicating with Uber Socket Service.
 * Service is discovered via Eureka with service name: UBER-SOCKET-SERVICE
 */
public interface UberSocketApi {

    /**
     * Sends a new ride request to the Socket Service to be broadcast to connected drivers.
     * 
     * @param requestDto The ride request details
     * @return Call<String> Response from the Socket Service
     */
    @POST("api/socket/newride")
    Call<String> raiseRideRequest(@Body RideRequestDto requestDto);
}

