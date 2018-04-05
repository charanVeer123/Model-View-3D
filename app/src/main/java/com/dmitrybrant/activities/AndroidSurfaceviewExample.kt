package com.dmitrybrant.activities

import android.app.Activity
import android.content.Intent
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.hardware.Camera.ShutterCallback
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

import com.dmitrybrant.modelviewer.R

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class AndroidSurfaceviewExample : AppCompatActivity(), SurfaceHolder.Callback {
    internal var testView: TextView? = null

    internal var camera: Camera? = null
    internal lateinit var surfaceView: SurfaceView
    internal lateinit var surfaceHolder: SurfaceHolder

    internal var rawCallback: PictureCallback? = null
    internal var shutterCallback: ShutterCallback? = null
    internal lateinit var jpegCallback: PictureCallback
    internal var EXTRA_CAMERA_DATA = "camera_data"

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)


        setContentView(R.layout.surface_view_exam)


        surfaceView = findViewById(R.id.surfaceView)
        surfaceHolder = surfaceView.holder
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        jpegCallback = PictureCallback { data, camera ->
            var outStream: FileOutputStream? = null
            try {
                val file = File(Environment.getExternalStorageDirectory().toString() + "/" + System.currentTimeMillis() + "CameraApp.jpg")
                if (!file.exists()) {
                    file.createNewFile()
                }
                outStream = FileOutputStream(String.format(Environment.getExternalStorageDirectory().toString() + "/" + file.name))
                outStream.write(data)
                outStream.close()

                val intent = Intent(this@AndroidSurfaceviewExample, ImagesGridActivity_3::class.java)
                intent.putExtra(EXTRA_CAMERA_DATA, data)
                //                    setResult(Activity.RESULT_OK, intent);

                startActivity(intent)
                finish()
                //   startActivity(new Intent(AndroidSurfaceviewExample.this,ImagesGridActivity_3.class));

                Log.d("Log", "onPictureTaken - wrote bytes: " + data.size)
                //                    sendData(file);

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
            }

            Toast.makeText(applicationContext, "Picture Saved", Toast.LENGTH_SHORT).show()
            //                refreshCamera();
        }
    }


    fun sendData(file: File) {
        //        new Thread(new Runnable() {
        //            @Override
        //            public void run() {
        //
        //                OkHttpClient client = new OkHttpClient();
        //                RequestBody requestBody  = RequestBody.create(MediaType.parse("image/*"),file);
        //
        //                Request request = new Request.Builder()
        //
        //                        .url("https://a3dyou.com:9000/images/right?uuid=076e161b-11a5-45b1-9391-af36a836121f")
        //                        .post(requestBody)
        //                        .addHeader("cache-control", "no-cache")
        //                        .addHeader("postman-token", "52013a94-e9d8-c811-e25c-05d6a4dc567e")
        //                        .build();
        //                try {
        //
        //                    okhttp3.Response response = client.newCall(request).execute();
        //                    response.body().string();
        //
        //                } catch (IOException e) {
        //                    e.printStackTrace();
        //                }
        //            }
        //        }).start();


        //
        //        new Thread(new Runnable() {
        //            @Override
        //            public void run() {
        //                String url = "https://a3dyou.com:9000/images/right?uuid=076e161b-11a5-45b1-9391-af36a836121f";
        //
        //                try {
        //                    HttpClient httpclient = new DefaultHttpClient();
        //
        //                    HttpPost httppost = new HttpPost(url);
        //
        //                    InputStreamEntity reqEntity = new InputStreamEntity(
        //                            new FileInputStream(file), -1);
        //                    reqEntity.setContentType("binary/octet-stream");
        //                    reqEntity.setChunked(true); // Send in multiple parts if needed
        //                    httppost.setEntity(reqEntity);
        //                    HttpResponse response = httpclient.execute(httppost);
        //                    Log.i("response","");
        //                    //Do something with response...
        //
        //                } catch (Exception e) {
        //                    // show error
        //                    e.printStackTrace();
        //                }
        //
        //
        //            }
        //        }).start();
    }


    @Throws(IOException::class)
    fun captureImage(v: View) {

        //take the picture

        camera!!.takePicture(null, null, jpegCallback)

    }


    fun refreshCamera() {

        if (surfaceHolder.surface == null) {
            // preview surface does not exist
            return
        }


        // stop preview before making changes

        try {
            camera!!.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }


        // set preview size and make any resize, rotate or


        // start preview with new settings

        try {
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.startPreview()
        } catch (e: Exception) {


        }

    }


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {

        // Now that the size is known, set up the camera parameters and begin

        // the preview.

        refreshCamera()

    }


    override fun surfaceCreated(holder: SurfaceHolder) {

        try {
            // open the camera
            camera = Camera.open()

        } catch (e: RuntimeException) {
            // check for exceptions
            System.err.println(e)
            return

        }

        val param: Camera.Parameters

        param = camera!!.parameters


        // modify parameter

        param.setPreviewSize(352, 288)

        camera!!.parameters = param

        try {
            // The Surface has been created, now tell the camera where to draw

            // the preview.
            setCameraDisplayOrientation(this@AndroidSurfaceviewExample, 1, camera!!)
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.startPreview()
        } catch (e: Exception) {
            // check for exceptions
            System.err.println(e)
            return
        }

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // stop preview and release camera
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }

    companion object {


        fun setCameraDisplayOrientation(activity: Activity,
                                        cameraId: Int, camera: android.hardware.Camera) {

            val info = android.hardware.Camera.CameraInfo()

            android.hardware.Camera.getCameraInfo(cameraId, info)

            val rotation = activity.windowManager.defaultDisplay.rotation
            var degrees = 0

            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }

            var result: Int
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360
            }
            camera.setDisplayOrientation(result)
        }
    }

}
