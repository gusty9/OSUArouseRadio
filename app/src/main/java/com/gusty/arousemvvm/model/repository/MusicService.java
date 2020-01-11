package com.gusty.arousemvvm.model.repository;

import com.gusty.arousemvvm.model.RecentTracks;
import com.gusty.arousemvvm.utility.Constants;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MusicService {
    String URL = Constants.Companion.getAPI_URL();

    //get at the '.' path uses the base url
    @GET(".")
    Call<RecentTracks> getTrack(@Query("") String s);
}
