package com.dmitrybrant.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.dmitrybrant.RetrofitLibrary.RetrofitLibrary;
import com.dmitrybrant.response.LeftImageResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ApiCalling extends Activity{


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final RetrofitLibrary.GitApiInterface restClient = RetrofitLibrary.getClient();


  /*      restClient.isUUID().enqueue(new Callback<UUIDResponse>() {
            @Override
            public void onResponse(Call<UUIDResponse> call, Response<UUIDResponse> response) {


                if(response.isSuccessful()){

                }
            }

            @Override
            public void onFailure(Call<UUIDResponse> call, Throwable t) {

            }
        });*/


  restClient.leftImage().enqueue(new Callback<LeftImageResponse>() {
      @Override
      public void onResponse(Call<LeftImageResponse> call, Response<LeftImageResponse> response) {

          if(response.isSuccessful()){

          }
      }

      @Override
      public void onFailure(Call<LeftImageResponse> call, Throwable t) {

      }
  });

    }
}
