package com.dmitrybrant;

import android.content.Context;
import android.util.Log;

import com.dmitrybrant.response.sessionResponse.CreateSessionRes;
import com.dmitrybrant.response.sessionResponse.DeleteSessionRes;
import com.dmitrybrant.retrofitLibrary.RetrofitLibrary;
import com.dmitrybrant.sharedPreferecnes.SharedPreferencesClass;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by dharamveer on 5/4/18.
 */

public class Utility {


    Context context;


    SharedPreferencesClass sharedPreferencesClass;




    public Utility(Context context) {
        this.context = context;

        new SharedPreferencesClass(context);
    }



    public  void  createSession1(){

        RetrofitLibrary.GitApiInterface restClient = RetrofitLibrary.getClient();

        restClient.createSession().enqueue(new Callback<CreateSessionRes>() {
            @Override
            public void onResponse(Call<CreateSessionRes> call, Response<CreateSessionRes> response) {


                if(response.code()==201){


                    Log.d("create session","201 OK (_uuid_ is response body)");

                    if(response.isSuccessful()){

                        sharedPreferencesClass.setSession_key(response.body().toString());

                    }

                }
                else if(response.code()==500){
                    Log.d("create session","500 Internal Server Error");

                }
                else if(response.code()==503){
                    Log.d("create session","503 Service Unavailable (if any session has already created and used)");

                }
                else
                {
                    sharedPreferencesClass.setSession_key("123456");
                }

            }

            @Override
            public void onFailure(Call<CreateSessionRes> call, Throwable t) {
                Log.d("failure delete session",t.getMessage());

            }
        });

    }





    public void deleteSession(){

        RetrofitLibrary.GitApiInterface restClient = RetrofitLibrary.getClient();

        restClient.deleteSession(RetrofitLibrary.GitApiInterface.session_key).enqueue(new Callback<DeleteSessionRes>() {
            @Override
            public void onResponse(Call<DeleteSessionRes> call, Response<DeleteSessionRes> response) {

                if(response.code()==200){

                    Log.d("delete session","200 ok");


                    if(response.isSuccessful()){
                        sharedPreferencesClass.setSession_key("123");
                    }

                }
                else if(response.code()==400){
                    Log.d("delete session","400 Bad Request (no 'uuid' query)");

                }
                else if(response.code()==404){
                    Log.d("delete session","404 Not Found");

                }
                else if(response.code()==500){
                    Log.d("delete session","500 Internal Server Error");

                }



            }

            @Override
            public void onFailure(Call<DeleteSessionRes> call, Throwable t) {
                Log.d("failure delete session",t.getMessage());

            }
        });

    }



}
