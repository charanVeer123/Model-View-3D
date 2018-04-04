package com.dmitrybrant.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dmitrybrant.RetrofitLibrary.RetrofitLibrary;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ApiCalling extends Activity{


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final RetrofitLibrary.GitApiInterface restClient = RetrofitLibrary.getClient();


        new Thread(new Runnable() {
            @Override
            public void run() {


                doMultiPartRequest();

            }
        }).start();



    }

    private void doMultiPartRequest() {

        String path = Environment.getExternalStorageDirectory().toString()+"/Pictures";
        Log.d("Files", "Path: "+path);
        File f = new File(path);

        DoActualRequest(f);


        File file[] = f.listFiles();
        Log.d("Files","Size: "+file.length);
        for(int i = 0 ; i < file.length;i++){
            if(file[i].isFile()){
                Log.d("OKHTTP3","FileName: " + file[i].getName());
                DoActualRequest(file[i]);
                break;
            }
        }
    }

    private void DoActualRequest(File file) {

        OkHttpClient client = new OkHttpClient();


        Log.d("OKHTTP3_FILES","Called Actual request");


        String url = "https://a3dyou.com:9000/images/left?uuid=8f1bc972-84cf-4106-b019-f9a1a5a728cf";
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"),file))
                .build();

        Log.d("OKHTTP3_FILES","Request body generated");


        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try {

            client.writeTimeoutMillis();
            Response response = client.newCall(request).execute();

            Log.d("OKHTTP3_FILES","Response successfull");
            Log.d("OKHTTP3_FILES",response.body().string());


        } catch (IOException e) {
            Log.d("OKHTTP3_FILES",e.getMessage());

            e.printStackTrace();
        }


    }


}
