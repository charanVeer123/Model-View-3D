package com.dmitrybrant;

import android.content.Context;
import android.widget.Toast;

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


    static Context context;

    public static RetrofitLibrary.GitApiInterface restClient = RetrofitLibrary.getClient();
    static SharedPreferencesClass sharedPreferencesClass;

    public Utility(Context context) {
        this.context = context;

    }

    public static void  createSession1(){

        restClient.createSession().enqueue(new Callback<CreateSessionRes>() {
            @Override
            public void onResponse(Call<CreateSessionRes> call, Response<CreateSessionRes> response) {


                if(response.code()==201){

                    Toast.makeText(context,"OK (_uuid_ is response body)",Toast.LENGTH_SHORT).show();


                    if(response.isSuccessful()){


                        sharedPreferencesClass = new SharedPreferencesClass(context);

                        sharedPreferencesClass.setSession_key(response.body().toString());

                    }
                    else
                    {


                    }
                }
                else if(response.code()==500){
                    Toast.makeText(context,"Internal Server Error",Toast.LENGTH_SHORT).show();

                }
                else if(response.code()==503){
                    Toast.makeText(context,"Service Unavailable (if any session has already created and used)",Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<CreateSessionRes> call, Throwable t) {
                Toast.makeText(context,t.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

    }


    public static void deleteSession(){


        restClient.deleteSession().enqueue(new Callback<DeleteSessionRes>() {
            @Override
            public void onResponse(Call<DeleteSessionRes> call, Response<DeleteSessionRes> response) {

                if(response.code()==200){

                    Toast.makeText(context,"OK",Toast.LENGTH_SHORT).show();


                    if(response.isSuccessful()){


                        sharedPreferencesClass.setSession_key("");

                    }
                    else
                    {


                    }
                }
                else if(response.code()==400){
                    Toast.makeText(context,"Bad Request (no 'uuid' query)",Toast.LENGTH_SHORT).show();

                }
                else if(response.code()==404){
                    Toast.makeText(context,"Not Found",Toast.LENGTH_SHORT).show();

                }
                else if(response.code()==500){
                    Toast.makeText(context,"Internal Server Error",Toast.LENGTH_SHORT).show();

                }



            }

            @Override
            public void onFailure(Call<DeleteSessionRes> call, Throwable t) {
                Toast.makeText(context,t.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

    }



}
