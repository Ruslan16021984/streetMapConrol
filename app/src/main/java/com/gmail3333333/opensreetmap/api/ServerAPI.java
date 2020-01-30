package com.gmail3333333.opensreetmap.api;

import com.gmail3333333.opensreetmap.constants.Constants;
import com.gmail3333333.opensreetmap.model.Basslocation;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ServerAPI {
    @GET("some")
    Observable<List<Basslocation>> loadListMessage();

    @POST("delete")
    Call<Basslocation> delete(@Body Basslocation message);
    @GET("getBasById/{id}")
    Observable<Basslocation> getBasById(@Path("id") int remindID);

    Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.URL.HOST_SCHOOL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

}
