package com.dmitrybrant.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.dmitrybrant.RetrofitLibrary.RetrofitLibrary;
import com.dmitrybrant.response.uploadImagesConfigRes.ConfigGenderHeight;
import com.dmitrybrant.response.uploadImagesConfigRes.LeftImageConfigRes;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;


public class ApiCalling extends Activity{

    private SensorManager mSensorManager;
    private Sensor mSensorX;
    private Sensor mSensorY;
    private Sensor mSensorZ;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final RetrofitLibrary.GitApiInterface restClient = RetrofitLibrary.getClient();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            /*Start-------------------Points Object-----------------------------Starts*/
            JSONObject jsonObjectTop  = new JSONObject();
            try {
                jsonObjectTop.put("x",2.5);
                jsonObjectTop.put("y",2.5);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            JSONObject jsonObjectBottom  = new JSONObject();
            try {
                jsonObjectBottom.put("x",2.5);
                jsonObjectBottom.put("y",2.5);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            JSONObject jsonObjectPoints  = new JSONObject();
            try {
                jsonObjectPoints.put("top",jsonObjectTop);
                jsonObjectPoints.put("bottom",jsonObjectBottom);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            /*End-------------------Points Object-----------------------------End*/

            /**********************************/



            /*Start-------------------intrinsics Object-----------------------------Starts*/
            //Intrinsics Object

            final CameraCharacteristics.Key<float[]> lensIntrinsicCalibration = CameraCharacteristics.LENS_INTRINSIC_CALIBRATION;

            CameraManager manager =
                    (CameraManager)getSystemService(CAMERA_SERVICE);
            CameraCharacteristics chars = null;

            try {
                assert manager != null;
                for (String cameraId : manager.getCameraIdList()) {
                    chars  = manager.getCameraCharacteristics(cameraId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            assert chars != null;
            float[] facing = chars.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION);



            JSONObject jsonObjectK  = new JSONObject();
            try {
                jsonObjectK.put("fx",2.5);
                jsonObjectK.put("fy",2.5);
                jsonObjectK.put("cx",2.5);
                jsonObjectK.put("cy",2.5);
                jsonObjectK.put("skew",2.5);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            JSONObject jsonObjectDistortion  = new JSONObject();
            try {
                jsonObjectDistortion.put("k1",2.5);
                jsonObjectDistortion.put("k2",2.5);
                jsonObjectDistortion.put("k3",2.5);
                jsonObjectDistortion.put("k4",2.5);
                jsonObjectDistortion.put("p1",2.5);
                jsonObjectDistortion.put("p2",2.5);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject jsonObjectIntrinsics  = new JSONObject();

            try {
                jsonObjectIntrinsics.put("K",jsonObjectK);
                jsonObjectIntrinsics.put("distortion",jsonObjectDistortion);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*End-------------------intrinsics Object-----------------------------End*/


            /**********************************/

            /*Start-------------------extrinsics Object-----------------------------Starts*/


            // 1. [Using the Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-rotate)
            //X values
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mSensorX = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


            //[Using the Game Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-gamerot)
            //y values
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mSensorY = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);


            //[Using the Geomagnetic Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-geomrot)
            //z values
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mSensorZ = mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);

            //Extrinsics Object
            JSONObject jsonObjectRotationVec  = new JSONObject();
            try {
                jsonObjectRotationVec.put("x",1.5);
                jsonObjectRotationVec.put("y",1.5);
                jsonObjectRotationVec.put("z",1.5);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject jsonObjectExtrinsics  = new JSONObject();

            try {
                jsonObjectExtrinsics.put("rotation vector component",jsonObjectRotationVec);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            /*End-------------------extrinsics Object-----------------------------End*/


            /**********************************/




            JSONObject jsonObjectRoot = new JSONObject();

            try {
                jsonObjectRoot.put("points",jsonObjectPoints);
                jsonObjectRoot.put("intrinsics",jsonObjectIntrinsics);
                jsonObjectRoot.put("extrinsics",jsonObjectExtrinsics);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            restClient.leftImageConfig(jsonObjectRoot).enqueue(new Callback<LeftImageConfigRes>() {
                @Override
                public void onResponse(Call<LeftImageConfigRes> call, retrofit2.Response<LeftImageConfigRes> response) {

                    if(response.code()==201){

                        if(response.isSuccessful())
                            Toast.makeText(ApiCalling.this, "Success", Toast.LENGTH_SHORT).show();

                        Toast.makeText(ApiCalling.this, "OK", Toast.LENGTH_SHORT).show();

                    }

                    else if(response.code()==400){
                        Toast.makeText(ApiCalling.this, "Bad Request (no 'uuid' query or json data could not be read)", Toast.LENGTH_SHORT).show();

                    }
                    else if(response.code()==404){
                        Toast.makeText(ApiCalling.this, "404 Not Found", Toast.LENGTH_SHORT).show();

                    }
                    else if(response.code()==409){
                        Toast.makeText(ApiCalling.this, "409 Conflict (json data has already loaded)", Toast.LENGTH_SHORT).show();

                    }
                    else if(response.code()==500){
                        Toast.makeText(ApiCalling.this, "500 Internal Server Error", Toast.LENGTH_SHORT).show();

                    }

                    else
                    {
                        Toast.makeText(ApiCalling.this, "False", Toast.LENGTH_SHORT).show();

                    }

                }

                @Override
                public void onFailure(Call<LeftImageConfigRes> call, Throwable t) {
                    Toast.makeText(ApiCalling.this, t.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });



            restClient.genderHeight().enqueue(new Callback<ConfigGenderHeight>() {
                @Override
                public void onResponse(Call<ConfigGenderHeight> call, retrofit2.Response<ConfigGenderHeight> response) {


                    if(response.code()==201){

                        if(response.isSuccessful())
                            Toast.makeText(ApiCalling.this, "Success", Toast.LENGTH_SHORT).show();

                        Toast.makeText(ApiCalling.this, "OK", Toast.LENGTH_SHORT).show();

                    }

                    else if(response.code()==400){
                        Toast.makeText(ApiCalling.this, "400 Bad Request (no 'uuid' query or json data could not be read)", Toast.LENGTH_SHORT).show();

                    }
                    else if(response.code()==404){
                        Toast.makeText(ApiCalling.this, "404 Not Found", Toast.LENGTH_SHORT).show();

                    }
                    else if(response.code()==409){
                        Toast.makeText(ApiCalling.this, "409 Conflict (json data has already loaded)", Toast.LENGTH_SHORT).show();

                    }
                    else if(response.code()==500){
                        Toast.makeText(ApiCalling.this, "500 Internal Server Error", Toast.LENGTH_SHORT).show();

                    }

                    else
                    {
                        Toast.makeText(ApiCalling.this, "False", Toast.LENGTH_SHORT).show();

                    }


                }

                @Override
                public void onFailure(Call<ConfigGenderHeight> call, Throwable t) {
                    Toast.makeText(ApiCalling.this, t.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });


        }

    }


}
