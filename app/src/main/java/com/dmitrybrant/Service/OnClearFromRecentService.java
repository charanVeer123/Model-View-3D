package com.dmitrybrant.Service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.dmitrybrant.Utility;
import java.lang.String;
import com.dmitrybrant.retrofitLibrary.RetrofitLibrary;
import com.orhanobut.hawk.Hawk;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OnClearFromRecentService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ClearFromRecentService", "Service Started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ClearFromRecentService", "Service Destroyed");

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Log.e("ClearFromRecentService", "END");


        deleteSession();

    }

    public void deleteSession(){

        RetrofitLibrary.GitApiInterface restClient = RetrofitLibrary.getClient();

        Hawk.delete("session_key");


        restClient.deleteSession(RetrofitLibrary.GitApiInterface.session_key).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if(response.code()==200){

                    Log.d("delete session","200 ok");

                    Toast.makeText(getApplicationContext(),"200 Ok",Toast.LENGTH_SHORT).show();
                    if(response.isSuccessful()){
                        Utility.sessionKey = null;
                    }

                }
                else if(response.code()==400){
                    Log.d("delete session","400 Bad Request (no 'uuid' query)");
                    Toast.makeText(getApplicationContext(),"400 Bad Request (no 'uuid' query)",Toast.LENGTH_SHORT).show();
                }
                else if(response.code()==404){
                    Log.d("delete session","404 Not Found");
                    Toast.makeText(getApplicationContext(),"404 Not Found",Toast.LENGTH_SHORT).show();
                }
                else if(response.code()==500){
                    Log.d("delete session","500 Internal Server Error");
                    Toast.makeText(getApplicationContext(),"500 Internal Server Error",Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

                try {
                    Log.d("failure delete session",t.getMessage());
                    Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
                } finally {
                    Toast.makeText(getApplicationContext(),"Unknown failure",Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

}