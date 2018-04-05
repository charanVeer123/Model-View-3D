package com.dmitrybrant.retrofitLibrary;

import com.dmitrybrant.Utility;
import com.dmitrybrant.response.sessionResponse.DeleteSessionRes;
import com.dmitrybrant.response.uploadImagesConfigRes.BackImageConfigRes;
import com.dmitrybrant.response.uploadImagesConfigRes.FrontImageConfigRes;
import com.dmitrybrant.response.uploadImagesConfigRes.RightImageConfigRes;
import com.dmitrybrant.response.uploadImagesServerRes.BackImageResponse;
import com.dmitrybrant.response.uploadImagesConfigRes.ConfigGenderHeight;
import com.dmitrybrant.response.uploadImagesServerRes.FrontImageResponse;
import com.dmitrybrant.response.uploadImagesConfigRes.LeftImageConfigRes;
import com.dmitrybrant.response.uploadImagesServerRes.LeftImageResponse;
import com.dmitrybrant.response.uploadImagesServerRes.RightImageResponse;
import com.dmitrybrant.response.sessionResponse.CreateSessionRes;
import com.dmitrybrant.sharedPreferecnes.SharedPreferencesClass;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class RetrofitLibrary {


    private static GitApiInterface gitApiInterface;

    // http://employeelive.com/kwiqmall/API/public/getRestaurants

    private static String baseUrl = "https://a3dyou.com:9000/";
    private static String session_key;




    public static GitApiInterface getClient() {


//        session_key = SharedPreferencesClass.getSession_key();

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


        //Create Session Api
        @GET("uuid")
        Call<CreateSessionRes> createSession();




        //Send images to server api's
        @Multipart
        @POST("images/back?uuid={session_key}")
        Call<LeftImageResponse> uploadleftImage(@Part("image\"; filename=\"pp.png\" ") RequestBody file);

        //Call<LeftImageResponse> uploadleftImage(@Part MultipartBody.Part filePart);


        @Multipart
        @POST("images/right?uuid={session_key}")
        Call<RightImageResponse> uploadrightImage(@Part("image\"; filename=\"pp.png\" ") RequestBody file);

        @Multipart
        @POST("images/front?uuid={session_key}")
        Call<FrontImageResponse> uploadfrontImage(@Part("image\"; filename=\"pp.png\" ") RequestBody file);

        @Multipart
        @POST("images/back?uuid={session_key}")
        Call<BackImageResponse> uploadbackImage(@Part("image\"; filename=\"pp.png\" ") RequestBody file);



        //Configuration Api's
        @POST("configuration/left?uuid={session_key}")
        Call<LeftImageConfigRes> leftImageConfig(@Body JSONObject jsonObject);


        //Configuration Api's
        @POST("configuration/right?uuid={session_key}")
        Call<RightImageConfigRes> rightImageConfig(@Body JSONObject jsonObject);

        //Configuration Api's
        @POST("configuration/front?uuid={session_key}")
        Call<FrontImageConfigRes> frontImageConfig(@Body JSONObject jsonObject);

        //Configuration Api's
        @POST("configuration/back?uuid={session_key}")
        Call<BackImageConfigRes> backImageConfig(@Body JSONObject jsonObject);



       // https://a3dyou.com:9000/configuration?uuid=session_key&gender=male&height=200
        @POST("configuration?uuid={session_key}&gender=female&height=400")
        Call<ConfigGenderHeight> genderHeight();


        //Delete session api
        @DELETE("uuid?uuid={session_key}")
        Call<DeleteSessionRes> deleteSession();



    }
}