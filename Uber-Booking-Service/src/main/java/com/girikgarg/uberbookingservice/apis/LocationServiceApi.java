package com.girikgarg.uberbookingservice.apis;

import com.girikgarg.uberbookingservice.dto.DriverLocationDto;
import com.girikgarg.uberbookingservice.dto.NearbyDriversRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;

/**
 * Retrofit interface for communicating with the Location Service.
 * 
 * This interface uses Retrofit to create a declarative REST client.
 * The actual HTTP calls are handled by Retrofit with OkHttp.
 * 
 * Usage:
 * 1. Create a Retrofit instance with base URL in configuration
 * 2. Use retrofit.create(LocationServiceApi.class) to get implementation
 * 3. Call methods which return Call<T> for async execution
 * 
 * Note: For service discovery integration, you can retrieve the base URL
 * from EurekaClient or use hardcoded URL from application.properties
 */
public interface LocationServiceApi {

    /**
     * Fetch nearby drivers within a specified radius from given coordinates.
     * 
     * @param request Contains latitude and longitude of the passenger's location
     * @return Retrofit Call object containing array of nearby drivers with their locations
     */
    @POST("/api/location/nearby/drivers")
    Call<DriverLocationDto[]> getNearbyDrivers(@Body NearbyDriversRequestDto request);
}

