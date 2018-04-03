package com.dmitrybrant.RetrofitLibrary;

import com.dmitrybrant.response.LeftImageResponse;
import com.dmitrybrant.response.UUIDResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class RetrofitLibrary {


    private static GitApiInterface gitApiInterface;

    // http://employeelive.com/kwiqmall/API/public/getRestaurants

    private static String baseUrl = "https://a3dyou.com:9000/";

    public static GitApiInterface getClient() {


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())

                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .build();


        gitApiInterface = retrofit.create(GitApiInterface.class);

        return gitApiInterface;


    }

    public interface GitApiInterface {



        @GET("uuid")
        Call<UUIDResponse> isUUID();


        @POST("images/left?uuid=8f1bc972-84cf-4106-b019-f9a1a5a728cf")
        Call<LeftImageResponse> leftImage();

/*

       //------------------------1
       as @POST("isPhoneNoExist")
        Call<PhoneNoSuccess> isPhoneNoExist(@Body HashMap<String, String> hhMap);


        //------------------------10
        @POST("validateOTP")
        Call<ValidateOTPSuccess> validateOTP(@Body HashMap<String, String> hashMap);




        @POST("updateLatLng")
        Call<UpdateLatLong> updateLatLng(@Body HashMap<String, String> hashMap);

*/



    }
}