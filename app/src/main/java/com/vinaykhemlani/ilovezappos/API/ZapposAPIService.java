package com.vinaykhemlani.ilovezappos.API;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Vinay on 2/1/17.
 * Interface used to implement Retrofit client
 */

public interface ZapposAPIService {

    static final String BASE_URL = "https://api.zappos.com/";

    static final String API_KEY = "b743e26728e16b81da139182bb2094357c31d331";
    static final String URL_SUFFIX = "Search?&key=";

    @GET(URL_SUFFIX + API_KEY)
    Call<APIResponse> performSearch(@Query("term") String term);

}
