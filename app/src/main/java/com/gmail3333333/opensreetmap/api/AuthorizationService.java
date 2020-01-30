package com.gmail3333333.opensreetmap.api;

import com.gmail3333333.opensreetmap.constants.Constants;
import com.gmail3333333.opensreetmap.model.User;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthorizationService {

    @GET("/findByPassword/{password}")
    Call<Boolean> getUser(@Path("password") String password);

    @POST("/putOnPassword")
    Call<User> saveUser(@Body User user);


    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constants.URL.HOST_SCHOOL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
