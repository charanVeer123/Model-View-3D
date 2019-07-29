package com.dmitrybrant.retrofitLibrary;

import com.dmitrybrant.Utility;
import com.dmitrybrant.response.sessionResponse.CreateSessionRes;
import com.dmitrybrant.response.sessionResponse.DeleteSessionRes;
import com.dmitrybrant.response.uploadImagesConfigRes.BackImageConfigRes;
import com.dmitrybrant.response.uploadImagesConfigRes.ConfigGenderHeight;
import com.dmitrybrant.response.uploadImagesConfigRes.FrontImageConfigRes;
import com.dmitrybrant.response.uploadImagesConfigRes.LeftImageConfigRes;
import com.dmitrybrant.response.uploadImagesConfigRes.RightImageConfigRes;
import com.dmitrybrant.response.uploadImagesServerRes.BackImageResponse;
import com.dmitrybrant.response.uploadImagesServerRes.FrontImageResponse;
import com.dmitrybrant.response.uploadImagesServerRes.LeftImageResponse;
import com.dmitrybrant.response.uploadImagesServerRes.RightImageResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.hawk.Hawk;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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
import retrofit2.http.Query;

public class RetrofitLibrary {


    private static GitApiInterface gitApiInterface;

    // http://employeelive.com/kwiqmall/API/public/getRestaurants

    private static String baseUrl = "https://a3dyou.com:9000/";




    public static GitApiInterface getClient() {



      /*  if(SharedPreferencesClass.getSession_key()==null){
            session_key = "c604464b-12ab-4654-9438-50e2787a1e58";

        }
        else
        session_key = SharedPreferencesClass.getSession_key();
*/

        // c604464b-12ab-4654-9438-50e2787a1e58



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




//        String session_key = "c604464b-12ab-4654-9438-50e2787a1e58";

        String session_key = Hawk.get("session_key","noapikey");





        //Create Session Api
        @GET("uuid")
        Call<String> createSession();

        //Send images to server api's
        @Multipart
        @POST("images/left?")
        Call<String> uploadleftImage(@Part("image\"; filename=\"pp.png\" ") RequestBody file ,@Query("uuid") String key);


        @Multipart
        @POST("images/right?")
        Call<String> uploadrightImage(@Part("image\"; filename=\"pp.png\" ") RequestBody file,@Query("uuid") String key);

        @Multipart
        @POST("images/front?")
        Call<String> uploadfrontImage(@Part("image\"; filename=\"pp.png\" ") RequestBody file,@Query("uuid") String key);

        @Multipart
        @POST("images/back?")
        Call<String> uploadbackImage(@Part("image\"; filename=\"pp.png\" ") RequestBody file,@Query("uuid") String key);

        //Configuration Api's
        @POST("configuration/left?")
        Call<String> leftImageConfig(@Body JSONObject jsonObject,@Query("uuid") String key);


        //Configuration Api's
        @POST("configuration/right?")
        Call<String> rightImageConfig(@Body JSONObject jsonObject,@Query("uuid") String key);

        //Configuration Api's
        @POST("configuration/front?")
        Call<String> frontImageConfig(@Body JSONObject jsonObject,@Query("uuid") String key);

        //Configuration Api's
        @POST("configuration/back?")
        Call<String> backImageConfig(@Body JSONObject jsonObject,@Query("uuid") String key);



        // https://a3dyou.com:9000/configuration?uuid=session_key&gender=male&height=200
        @POST("configuration?")
        Call<String> genderHeight(@Query("uuid") String key,@Query("gender") String gender,@Query("height") String height);


        //Delete session api
        @DELETE("uuid?")
        Call<String> deleteSession(@Query("uuid") String key);



    }
}