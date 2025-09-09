package com.example.directions_app.network;

import com.example.directions_app.model.Address;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("autocomplete.php")
    Call<List<Address>> searchAddress(
            @Query("key") String apiKey,
            @Query("q") String query,
            @Query("format") String format,
            @Query("countrycodes") String countryCodes,
            @Query("limit") int limit
    );
}
