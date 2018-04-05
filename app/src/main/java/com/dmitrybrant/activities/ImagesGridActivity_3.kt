package com.dmitrybrant.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.dmitrybrant.retrofitLibrary.RetrofitLibrary
import com.dmitrybrant.models.ImagesModel
import com.dmitrybrant.modelviewer.MainActivityPlyParser
import com.dmitrybrant.modelviewer.R
import com.dmitrybrant.response.uploadImagesConfigRes.BackImageConfigRes
import com.dmitrybrant.response.uploadImagesConfigRes.FrontImageConfigRes
import com.dmitrybrant.response.uploadImagesConfigRes.LeftImageConfigRes
import com.dmitrybrant.response.uploadImagesConfigRes.RightImageConfigRes
import com.dmitrybrant.response.uploadImagesServerRes.BackImageResponse
import com.dmitrybrant.response.uploadImagesServerRes.FrontImageResponse
import com.dmitrybrant.response.uploadImagesServerRes.LeftImageResponse
import com.dmitrybrant.response.uploadImagesServerRes.RightImageResponse
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_captured_images.*
import kotlinx.android.synthetic.main.grid_item_layout.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dharamveer on 28/3/18.
 */
class ImagesGridActivity_3 : AppCompatActivity(), View.OnTouchListener {

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        val inverse = Matrix()
        imageViewGl!!.getImageMatrix().invert(inverse);

        val pts = floatArrayOf(event!!.getX(), event.getY())
        inverse.mapPoints(pts);


        return false;
    }


    var adapter: ImagesAdapter? = null
    var imagesList = ArrayList<ImagesModel>()
    lateinit var imageFilePath: String
    var imageViewGl: ImageView? = null
    private val TAKE_PICTURE_REQUEST_LEFT = 20
    private val TAKE_PICTURE_REQUEST_RIGHT = 30
    private val TAKE_PICTURE_REQUEST_FRONT = 40
    private val TAKE_PICTURE_REQUEST_BACK = 50
    private var mCameraBitmap: Bitmap? = null
    private var txtCreate: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        //Remove notification bar
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_captured_images)

        txtCreate = findViewById(R.id.txtCreate) as TextView


       val dialog = SpotsDialog(this,R.style.CustomProgressDialog)

        txtCreate!!.setOnClickListener{

            dialog.show()

            val mHandler = Handler()
            mHandler.postDelayed(Runnable {
                //start your activity here

                val intent = Intent(this, MainActivityPlyParser::class.java)
                startActivity(intent)
                dialog.dismiss()

            }, 1000L)



        }


        // load items

        imagesList.add(ImagesModel("Front View", R.drawable.front))
        imagesList.add(ImagesModel("Back View", R.drawable.back))
        imagesList.add(ImagesModel("Left View ", R.drawable.left))
        imagesList.add(ImagesModel("Right View",R.drawable.right))



        adapter = ImagesAdapter(this, imagesList);
        adapter!!.setOnItemClickListener(object : ImagesAdapter.CameraClickListener {
            override fun onCameraClick(position: Int,imageView: ImageView) {

                imageViewGl  = imageView


                if(position==0){
                    startImageCapture(TAKE_PICTURE_REQUEST_FRONT)

                }else if(position==1){
                    startImageCapture(TAKE_PICTURE_REQUEST_BACK)

                }
                else if(position==2){
                    startImageCapture(TAKE_PICTURE_REQUEST_LEFT)

                }
                else if(position==3){
                    startImageCapture(TAKE_PICTURE_REQUEST_RIGHT)

                }

            }

        })

        gridCapture.adapter = adapter

    }


    private fun startImageCapture( requestCode: Int) {
        // startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_REQUEST_B);
        startActivityForResult(Intent(this@ImagesGridActivity_3, CameraActivity::class.java),requestCode)


       /* val intent = Intent(this@ImagesGridActivity_3, AndroidSurfaceviewExample::class.java)

        startActivity(intent)
*/
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName: String = "JPEG_" + timeStamp + "-"
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if(!storageDir.exists()) storageDir.mkdir()
        val imageFile = File.createTempFile(imageFileName,".jpg",storageDir)
        imageFilePath  = imageFile.absolutePath
        return imageFile

    }


    private var mCapturedImageURI: Uri? = null
    val restClient = RetrofitLibrary.getClient()


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //For left image
        if (requestCode == TAKE_PICTURE_REQUEST_LEFT) {
            if (resultCode == Activity.RESULT_OK) {
                // Recycle the previous bitmap.

                if (mCameraBitmap != null && !mCameraBitmap!!.isRecycled()) {
                    // mCameraBitmap!!.recycle();
                    mCameraBitmap = null;
                }


                val extras = data?.extras
                val cameraData = extras!!.getByteArray(CameraActivity.EXTRA_CAMERA_DATA)

                if (cameraData != null) {
                    mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.size)

                    // val photo: Bitmap// this is your image.
                    val stream = ByteArrayOutputStream()
                    mCameraBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)



                    //creating request body for file

                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                    mCapturedImageURI = getImageUri(applicationContext, mCameraBitmap!!)

                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    val finalFile = File(getRealPathFromURI(mCapturedImageURI!!))


                   // val requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(mCapturedImageURI)!!), finalFile)
                   // val requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(mCapturedImageURI)!!), finalFile)
                    val requestFile = RequestBody.create(MediaType.parse("image/*"), finalFile)


                    //java.io.EOFException: End of input at line 1 column 1

                    //Api for left image
                    restClient.uploadleftImage(requestFile).enqueue(object : retrofit2.Callback<LeftImageResponse> {
                        override fun onResponse(call: Call<LeftImageResponse>, response: Response<LeftImageResponse>) {

                            if (response.code() == 201) run {

                                if (response.isSuccessful)
                                    Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()

                                Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 400){

                                Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or image could not be read)", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 404){

                                Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 409){

                                Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (image is still loading or has already loaded)", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 500){

                                Toast.makeText(this@ImagesGridActivity_3, "Internal Server Error", Toast.LENGTH_SHORT).show()

                            }
                        }

                        override fun onFailure(call: Call<LeftImageResponse>, t: Throwable) {
                            Toast.makeText(this@ImagesGridActivity_3,t.toString(),Toast.LENGTH_SHORT).show()

                        }
                    })

                    getJsonObjectleftImage()


                    imageViewGl!!.setImageBitmap(RotateBitmap(mCameraBitmap!!,90f))


                    mCameraBitmap!!.byteCount;


                    val saveFile = openFileForImage()
                    if (saveFile != null) {
                        saveImageToFile(saveFile)
                    } else {
                        Toast.makeText(this@ImagesGridActivity_3, "Unable to open file for saving image.",
                                Toast.LENGTH_LONG).show()
                    }



                    imageViewGl!!.setOnTouchListener(this)

                }
            } else {
                mCameraBitmap = null

            }

            //For right
        }

        //For right image
        else if(requestCode == TAKE_PICTURE_REQUEST_RIGHT){

            if (resultCode == Activity.RESULT_OK) {

                if (mCameraBitmap != null && !mCameraBitmap!!.isRecycled()) {
                    // mCameraBitmap!!.recycle();
                    mCameraBitmap = null;
                }

                val extras = data?.extras
                val cameraData = extras!!.getByteArray(CameraActivity.EXTRA_CAMERA_DATA)


                if (cameraData != null) {

                    mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.size)

                    // val photo: Bitmap// this is your image.
                    val stream = ByteArrayOutputStream()
                    mCameraBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)



                    //creating request body for file

                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                    mCapturedImageURI = getImageUri(applicationContext, mCameraBitmap!!)

                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    val finalFile = File(getRealPathFromURI(mCapturedImageURI!!))


                    //val requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(mCapturedImageURI)!!), finalFile)


                    val requestFile = RequestBody.create(MediaType.parse("image/*"), finalFile)


                    //Api for right image
                    restClient.uploadrightImage(requestFile).enqueue(object : retrofit2.Callback<RightImageResponse> {

                        override fun onResponse(call: Call<RightImageResponse>, response: Response<RightImageResponse>) {
                            if (response.code() == 201) run {

                                if (response.isSuccessful)
                                    Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()

                                Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 400){

                                Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or image could not be read)", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 404){

                                Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 409){

                                Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (image is still loading or has already loaded)", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 500){

                                Toast.makeText(this@ImagesGridActivity_3, "Internal Server Error", Toast.LENGTH_SHORT).show()

                            }
                        }

                        override fun onFailure(call: Call<RightImageResponse>?, t: Throwable?) {
                            Toast.makeText(this@ImagesGridActivity_3, t.toString(), Toast.LENGTH_SHORT).show()
                        }


                    })

                    getJsonObjectrightImage()

                    imageViewGl!!.setImageBitmap(RotateBitmap(mCameraBitmap!!,90f))


                    mCameraBitmap!!.byteCount;





                    val saveFile = openFileForImage()
                    if (saveFile != null) {
                        saveImageToFile(saveFile)
                    } else {
                        Toast.makeText(this@ImagesGridActivity_3, "Unable to open file for saving image.",
                                Toast.LENGTH_LONG).show()
                    }



                    imageViewGl!!.setOnTouchListener(this)

                }


            }

            else {
                mCameraBitmap = null

            }

        }

        //For front image
        else if(requestCode == TAKE_PICTURE_REQUEST_FRONT){

            if (resultCode == Activity.RESULT_OK) {

                if (mCameraBitmap != null && !mCameraBitmap!!.isRecycled()) {
                    // mCameraBitmap!!.recycle();
                    mCameraBitmap = null;
                }

                val extras = data?.extras
                val cameraData = extras!!.getByteArray(CameraActivity.EXTRA_CAMERA_DATA)

                if (cameraData != null) {

                    mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.size)

                    // val photo: Bitmap// this is your image.
                    val stream = ByteArrayOutputStream()
                    mCameraBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)

                    //creating request body for file

                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                    mCapturedImageURI = getImageUri(applicationContext, mCameraBitmap!!)

                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    val finalFile = File(getRealPathFromURI(mCapturedImageURI!!))


                    //val requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(mCapturedImageURI)!!), finalFile)
                    val requestFile = RequestBody.create(MediaType.parse("image/*"), finalFile)


                    //Api for front image
                    restClient.uploadfrontImage(requestFile).enqueue(object : retrofit2.Callback<FrontImageResponse> {

                        override fun onResponse(call: Call<FrontImageResponse>, response: Response<FrontImageResponse>) {

                            if (response.code() == 201) run {

                                if (response.isSuccessful)
                                    Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()

                                Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 400){

                                Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or image could not be read)", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 404){

                                Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 409){

                                Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (image is still loading or has already loaded)", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 500){

                                Toast.makeText(this@ImagesGridActivity_3, "Internal Server Error", Toast.LENGTH_SHORT).show()

                            }



                        }


                        override fun onFailure(call: Call<FrontImageResponse>?, t: Throwable?) {
                            Toast.makeText(this@ImagesGridActivity_3, t.toString(), Toast.LENGTH_SHORT).show()
                        }

                    })


                    getJsonObjectFront()


                    imageViewGl!!.setImageBitmap(RotateBitmap(mCameraBitmap!!,90f))



                    mCameraBitmap!!.byteCount;





                    val saveFile = openFileForImage()
                    if (saveFile != null) {
                        saveImageToFile(saveFile)
                    } else {
                        Toast.makeText(this@ImagesGridActivity_3, "Unable to open file for saving image.",
                                Toast.LENGTH_LONG).show()
                    }



                    imageViewGl!!.setOnTouchListener(this)





                }
            }

            else {
                mCameraBitmap = null

            }

        }

        //For back image
        else if(requestCode == TAKE_PICTURE_REQUEST_BACK){

            if (resultCode == Activity.RESULT_OK) {

                if (mCameraBitmap != null && !mCameraBitmap!!.isRecycled()) {
                    // mCameraBitmap!!.recycle();
                    mCameraBitmap = null;
                }

                val extras = data?.extras
                val cameraData = extras!!.getByteArray(CameraActivity.EXTRA_CAMERA_DATA)

                if (cameraData != null) {


                    mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.size)

                    // val photo: Bitmap// this is your image.
                    val stream = ByteArrayOutputStream()
                    mCameraBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)



                    //creating request body for file

                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                    mCapturedImageURI = getImageUri(applicationContext, mCameraBitmap!!)

                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    val finalFile = File(getRealPathFromURI(mCapturedImageURI!!))


                   // val requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(mCapturedImageURI)!!), finalFile)
                    val requestFile = RequestBody.create(MediaType.parse("image/*"), finalFile)


                    //Api for back image
                    restClient.uploadbackImage(requestFile).enqueue(object : retrofit2.Callback<BackImageResponse> {
                        override fun onResponse(call: Call<BackImageResponse>, response: Response<BackImageResponse>) {
                            if (response.code() == 201) run {

                                if (response.isSuccessful)
                                    Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()

                                Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 400){

                                Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or image could not be read)", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 404){

                                Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 409){

                                Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (image is still loading or has already loaded)", Toast.LENGTH_SHORT).show()

                            }
                            else if(response.code() == 500){

                                Toast.makeText(this@ImagesGridActivity_3, "Internal Server Error", Toast.LENGTH_SHORT).show()

                            }

                        }

                        override fun onFailure(call: Call<BackImageResponse>?, t: Throwable?) {
                            Toast.makeText(this@ImagesGridActivity_3, t.toString(), Toast.LENGTH_SHORT).show()
                        }

                    })


                    getJsonObjectbackImage()

                    imageViewGl!!.setImageBitmap(RotateBitmap(mCameraBitmap!!,90f))



                    mCameraBitmap!!.byteCount;





                    val saveFile = openFileForImage()
                    if (saveFile != null) {
                        saveImageToFile(saveFile)
                    } else {
                        Toast.makeText(this@ImagesGridActivity_3, "Unable to open file for saving image.",
                                Toast.LENGTH_LONG).show()
                    }



                    imageViewGl!!.setOnTouchListener(this)

                }
            }

            else {
                mCameraBitmap = null

            }
        }

    }



    private var mSensorManager: SensorManager? = null
    private var mSensorX: Sensor? = null
    private var mSensorY: Sensor? = null
    private var mSensorZ: Sensor? = null


    private fun getJsonObjectFront() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            /*Start-------------------Points Object-----------------------------Starts*/

            val jsonObjectTop = JSONObject()
            try {
                jsonObjectTop.put("x", 2.5)
                jsonObjectTop.put("y", 2.5)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectBottom = JSONObject()
            try {
                jsonObjectBottom.put("x", 2.5)
                jsonObjectBottom.put("y", 2.5)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectPoints = JSONObject()
            try {
                jsonObjectPoints.put("top", jsonObjectTop)
                jsonObjectPoints.put("bottom", jsonObjectBottom)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------Points Object-----------------------------End*/

            /**********************************/


            /*Start-------------------intrinsics Object-----------------------------Starts*/
            //Intrinsics Object

            //val lensIntrinsicCalibration = CameraCharacteristics.LENS_INTRINSIC_CALIBRATION

            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var chars: CameraCharacteristics? = null

            try {
                assert(manager != null)
                for (cameraId in manager.cameraIdList) {
                    chars = manager.getCameraCharacteristics(cameraId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


            assert(chars != null)
            val facing = chars!!.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION)

            val jsonObjectK = JSONObject()

            if (facing == null) {

                try {
                    jsonObjectK.put("fx", null)
                    jsonObjectK.put("fy", null)
                    jsonObjectK.put("cx", null)
                    jsonObjectK.put("cy", null)
                    jsonObjectK.put("skew", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {

                try {
                    jsonObjectK.put("fx", 2.5)
                    jsonObjectK.put("fy", 2.5)
                    jsonObjectK.put("cx", 2.5)
                    jsonObjectK.put("cy", 2.5)
                    jsonObjectK.put("skew", 2.5)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }




            var distortion: FloatArray? = FloatArray(4)

            val managerDistor = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var charsDistor: CameraCharacteristics? = null

            try {
                assert(managerDistor != null)
                for (cameraId in managerDistor.cameraIdList) {
                    charsDistor = managerDistor.getCameraCharacteristics(cameraId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            assert(charsDistor != null)


            distortion = charsDistor!!.get(CameraCharacteristics.LENS_RADIAL_DISTORTION)

            val jsonObjectDistortion = JSONObject()

            if (distortion == null) {

                try {
                    jsonObjectDistortion.put("k1", null)
                    jsonObjectDistortion.put("k2", null)
                    jsonObjectDistortion.put("k3", null)
                    jsonObjectDistortion.put("k4", null)
                    jsonObjectDistortion.put("p1", null)
                    jsonObjectDistortion.put("p2", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            } else {

                try {
                    jsonObjectDistortion.put("k1", null)
                    jsonObjectDistortion.put("k2", null)
                    jsonObjectDistortion.put("k3", null)
                    jsonObjectDistortion.put("k4", null)
                    jsonObjectDistortion.put("p1", null)
                    jsonObjectDistortion.put("p2", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            }

            val jsonObjectIntrinsics = JSONObject()

            try {
                jsonObjectIntrinsics.put("K", jsonObjectK)
                jsonObjectIntrinsics.put("distortion", jsonObjectDistortion)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------intrinsics Object-----------------------------End*/


            /**********************************/

            /*Start-------------------extrinsics Object-----------------------------Starts*/


            // 1. [Using the Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-rotate)
            //X values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorX = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)


            //[Using the Game Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-gamerot)
            //y values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorY = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)


            //[Using the Geomagnetic Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-geomrot)
            //z values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorZ = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

            //Extrinsics Object
            val jsonObjectRotationVec = JSONObject()

            if(mSensorX==null && mSensorY==null && mSensorZ==null){

                try {
                    jsonObjectRotationVec.put("x", null)
                    jsonObjectRotationVec.put("y", null)
                    jsonObjectRotationVec.put("z", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            }
            else {
                try {
                    jsonObjectRotationVec.put("x", mSensorX)
                    jsonObjectRotationVec.put("y", mSensorY)
                    jsonObjectRotationVec.put("z", mSensorZ)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }




            val jsonObjectExtrinsics = JSONObject()

            try {
                jsonObjectExtrinsics.put("rotation vector component", jsonObjectRotationVec)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------extrinsics Object-----------------------------End*/


            /**********************************/


            val jsonObjectRoot = JSONObject()

            try {
                jsonObjectRoot.put("points", jsonObjectPoints)
                jsonObjectRoot.put("intrinsics", jsonObjectIntrinsics)
                jsonObjectRoot.put("extrinsics", jsonObjectExtrinsics)
            } catch (e: JSONException) {
                e.printStackTrace()
            }




            restClient.frontImageConfig(jsonObjectRoot).enqueue(object : retrofit2.Callback<FrontImageConfigRes>{
                override fun onResponse(call: Call<FrontImageConfigRes>, response: Response<FrontImageConfigRes>) {

                    if (response.code() == 201) {

                        if (response.isSuccessful())
                            Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()

                        Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 400) {
                        Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or json data could not be read)", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 404) {
                        Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 409) {
                        Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (json data has already loaded)", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 500) {
                        Toast.makeText(this@ImagesGridActivity_3, "500 Internal Server Error", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this@ImagesGridActivity_3, "False", Toast.LENGTH_SHORT).show()

                    }

                }

                override fun onFailure(call: Call<FrontImageConfigRes>?, t: Throwable?) {

                }

            })
        }


    }
    private fun getJsonObjectleftImage() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            /*Start-------------------Points Object-----------------------------Starts*/

            val jsonObjectTop = JSONObject()
            try {
                jsonObjectTop.put("x", 2.5)
                jsonObjectTop.put("y", 2.5)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectBottom = JSONObject()
            try {
                jsonObjectBottom.put("x", 2.5)
                jsonObjectBottom.put("y", 2.5)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectPoints = JSONObject()
            try {
                jsonObjectPoints.put("top", jsonObjectTop)
                jsonObjectPoints.put("bottom", jsonObjectBottom)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------Points Object-----------------------------End*/

            /**********************************/


            /*Start-------------------intrinsics Object-----------------------------Starts*/
            //Intrinsics Object

            val lensIntrinsicCalibration = CameraCharacteristics.LENS_INTRINSIC_CALIBRATION

            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var chars: CameraCharacteristics? = null

            try {
                assert(manager != null)
                for (cameraId in manager.cameraIdList) {
                    chars = manager.getCameraCharacteristics(cameraId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


            assert(chars != null)
            val facing = chars!!.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION)

            val jsonObjectK = JSONObject()

            if (facing == null) {

                try {
                    jsonObjectK.put("fx", null)
                    jsonObjectK.put("fy", null)
                    jsonObjectK.put("cx", null)
                    jsonObjectK.put("cy", null)
                    jsonObjectK.put("skew", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {

                try {
                    jsonObjectK.put("fx", 2.5)
                    jsonObjectK.put("fy", 2.5)
                    jsonObjectK.put("cx", 2.5)
                    jsonObjectK.put("cy", 2.5)
                    jsonObjectK.put("skew", 2.5)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }


            var distortion: FloatArray? = FloatArray(4)

            val managerDistor = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var charsDistor: CameraCharacteristics? = null

            try {
                assert(managerDistor != null)
                for (cameraId in managerDistor.cameraIdList) {
                    charsDistor = managerDistor.getCameraCharacteristics(cameraId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            assert(charsDistor != null)


            distortion = charsDistor!!.get(CameraCharacteristics.LENS_RADIAL_DISTORTION)

            val jsonObjectDistortion = JSONObject()

            if (distortion == null) {

                try {
                    jsonObjectDistortion.put("k1", null)
                    jsonObjectDistortion.put("k2", null)
                    jsonObjectDistortion.put("k3", null)
                    jsonObjectDistortion.put("k4", null)
                    jsonObjectDistortion.put("p1", null)
                    jsonObjectDistortion.put("p2", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            } else {

                try {
                    jsonObjectDistortion.put("k1", null)
                    jsonObjectDistortion.put("k2", null)
                    jsonObjectDistortion.put("k3", null)
                    jsonObjectDistortion.put("k4", null)
                    jsonObjectDistortion.put("p1", null)
                    jsonObjectDistortion.put("p2", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            }

            val jsonObjectIntrinsics = JSONObject()

            try {
                jsonObjectIntrinsics.put("K", jsonObjectK)
                jsonObjectIntrinsics.put("distortion", jsonObjectDistortion)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------intrinsics Object-----------------------------End*/


            /**********************************/

            /*Start-------------------extrinsics Object-----------------------------Starts*/


            // 1. [Using the Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-rotate)
            //X values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorX = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)


            //[Using the Game Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-gamerot)
            //y values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorY = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)


            //[Using the Geomagnetic Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-geomrot)
            //z values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorZ = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

            //Extrinsics Object
            val jsonObjectRotationVec = JSONObject()
            try {
                jsonObjectRotationVec.put("x", mSensorX)
                jsonObjectRotationVec.put("y", mSensorY)
                jsonObjectRotationVec.put("z", mSensorZ)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectExtrinsics = JSONObject()

            try {
                jsonObjectExtrinsics.put("rotation vector component", jsonObjectRotationVec)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------extrinsics Object-----------------------------End*/


            /**********************************/


            val jsonObjectRoot = JSONObject()

            try {
                jsonObjectRoot.put("points", jsonObjectPoints)
                jsonObjectRoot.put("intrinsics", jsonObjectIntrinsics)
                jsonObjectRoot.put("extrinsics", jsonObjectExtrinsics)
            } catch (e: JSONException) {
                e.printStackTrace()
            }




            restClient.leftImageConfig(jsonObjectRoot).enqueue(object : retrofit2.Callback<LeftImageConfigRes>{
                override fun onFailure(call: Call<LeftImageConfigRes>, t: Throwable) {
                    Toast.makeText(this@ImagesGridActivity_3, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<LeftImageConfigRes>, response: Response<LeftImageConfigRes>) {
                    if (response.code() == 201) {

                        if (response.isSuccessful())
                            Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()

                        Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 400) {
                        Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or json data could not be read)", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 404) {
                        Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 409) {
                        Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (json data has already loaded)", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 500) {
                        Toast.makeText(this@ImagesGridActivity_3, "500 Internal Server Error", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this@ImagesGridActivity_3, "False", Toast.LENGTH_SHORT).show()

                    }
                }

            })


        }

    }
    private fun getJsonObjectrightImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            /*Start-------------------Points Object-----------------------------Starts*/

            val jsonObjectTop = JSONObject()
            try {
                jsonObjectTop.put("x", 2.5)
                jsonObjectTop.put("y", 2.5)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectBottom = JSONObject()
            try {
                jsonObjectBottom.put("x", 2.5)
                jsonObjectBottom.put("y", 2.5)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectPoints = JSONObject()
            try {
                jsonObjectPoints.put("top", jsonObjectTop)
                jsonObjectPoints.put("bottom", jsonObjectBottom)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------Points Object-----------------------------End*/

            /**********************************/


            /*Start-------------------intrinsics Object-----------------------------Starts*/
            //Intrinsics Object

            val lensIntrinsicCalibration = CameraCharacteristics.LENS_INTRINSIC_CALIBRATION

            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var chars: CameraCharacteristics? = null

            try {
                assert(manager != null)
                for (cameraId in manager.cameraIdList) {
                    chars = manager.getCameraCharacteristics(cameraId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


            assert(chars != null)
            val facing = chars!!.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION)

            val jsonObjectK = JSONObject()

            if (facing == null) {

                try {
                    jsonObjectK.put("fx", null)
                    jsonObjectK.put("fy", null)
                    jsonObjectK.put("cx", null)
                    jsonObjectK.put("cy", null)
                    jsonObjectK.put("skew", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {

                try {
                    jsonObjectK.put("fx", 2.5)
                    jsonObjectK.put("fy", 2.5)
                    jsonObjectK.put("cx", 2.5)
                    jsonObjectK.put("cy", 2.5)
                    jsonObjectK.put("skew", 2.5)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }


            var distortion: FloatArray? = FloatArray(4)

            val managerDistor = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var charsDistor: CameraCharacteristics? = null

            try {
                assert(managerDistor != null)
                for (cameraId in managerDistor.cameraIdList) {
                    charsDistor = managerDistor.getCameraCharacteristics(cameraId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            assert(charsDistor != null)


            distortion = charsDistor!!.get(CameraCharacteristics.LENS_RADIAL_DISTORTION)

            val jsonObjectDistortion = JSONObject()

            if (distortion == null) {

                try {
                    jsonObjectDistortion.put("k1", null)
                    jsonObjectDistortion.put("k2", null)
                    jsonObjectDistortion.put("k3", null)
                    jsonObjectDistortion.put("k4", null)
                    jsonObjectDistortion.put("p1", null)
                    jsonObjectDistortion.put("p2", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            } else {

                try {
                    jsonObjectDistortion.put("k1", null)
                    jsonObjectDistortion.put("k2", null)
                    jsonObjectDistortion.put("k3", null)
                    jsonObjectDistortion.put("k4", null)
                    jsonObjectDistortion.put("p1", null)
                    jsonObjectDistortion.put("p2", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            }

            val jsonObjectIntrinsics = JSONObject()

            try {
                jsonObjectIntrinsics.put("K", jsonObjectK)
                jsonObjectIntrinsics.put("distortion", jsonObjectDistortion)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------intrinsics Object-----------------------------End*/


            /**********************************/

            /*Start-------------------extrinsics Object-----------------------------Starts*/


            // 1. [Using the Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-rotate)
            //X values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorX = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)


            //[Using the Game Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-gamerot)
            //y values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorY = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)


            //[Using the Geomagnetic Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-geomrot)
            //z values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorZ = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

            //Extrinsics Object
            val jsonObjectRotationVec = JSONObject()
            try {
                jsonObjectRotationVec.put("x", mSensorX)
                jsonObjectRotationVec.put("y", mSensorY)
                jsonObjectRotationVec.put("z", mSensorZ)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectExtrinsics = JSONObject()

            try {
                jsonObjectExtrinsics.put("rotation vector component", jsonObjectRotationVec)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------extrinsics Object-----------------------------End*/


            /**********************************/


            val jsonObjectRoot = JSONObject()

            try {
                jsonObjectRoot.put("points", jsonObjectPoints)
                jsonObjectRoot.put("intrinsics", jsonObjectIntrinsics)
                jsonObjectRoot.put("extrinsics", jsonObjectExtrinsics)
            } catch (e: JSONException) {
                e.printStackTrace()
            }




            restClient.rightImageConfig(jsonObjectRoot).enqueue(object : retrofit2.Callback<RightImageConfigRes>{
                override fun onFailure(call: Call<RightImageConfigRes>, t: Throwable) {
                    Toast.makeText(this@ImagesGridActivity_3, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<RightImageConfigRes>, response: Response<RightImageConfigRes>) {
                    if (response.code() == 201) {

                        if (response.isSuccessful())
                            Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()

                        Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 400) {
                        Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or json data could not be read)", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 404) {
                        Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 409) {
                        Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (json data has already loaded)", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 500) {
                        Toast.makeText(this@ImagesGridActivity_3, "500 Internal Server Error", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this@ImagesGridActivity_3, "False", Toast.LENGTH_SHORT).show()

                    }
                }


            })


        }


    }
    private fun getJsonObjectbackImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            /*Start-------------------Points Object-----------------------------Starts*/

            val jsonObjectTop = JSONObject()
            try {
                jsonObjectTop.put("x", 2.5)
                jsonObjectTop.put("y", 2.5)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectBottom = JSONObject()
            try {
                jsonObjectBottom.put("x", 2.5)
                jsonObjectBottom.put("y", 2.5)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectPoints = JSONObject()
            try {
                jsonObjectPoints.put("top", jsonObjectTop)
                jsonObjectPoints.put("bottom", jsonObjectBottom)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------Points Object-----------------------------End*/

            /**********************************/


            /*Start-------------------intrinsics Object-----------------------------Starts*/
            //Intrinsics Object

            val lensIntrinsicCalibration = CameraCharacteristics.LENS_INTRINSIC_CALIBRATION

            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var chars: CameraCharacteristics? = null

            try {
                assert(manager != null)
                for (cameraId in manager.cameraIdList) {
                    chars = manager.getCameraCharacteristics(cameraId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


            assert(chars != null)
            val facing = chars!!.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION)

            val jsonObjectK = JSONObject()

            if (facing == null) {

                try {
                    jsonObjectK.put("fx", null)
                    jsonObjectK.put("fy", null)
                    jsonObjectK.put("cx", null)
                    jsonObjectK.put("cy", null)
                    jsonObjectK.put("skew", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {

                try {
                    jsonObjectK.put("fx", 2.5)
                    jsonObjectK.put("fy", 2.5)
                    jsonObjectK.put("cx", 2.5)
                    jsonObjectK.put("cy", 2.5)
                    jsonObjectK.put("skew", 2.5)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }


            var distortion: FloatArray? = FloatArray(4)

            val managerDistor = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var charsDistor: CameraCharacteristics? = null

            try {
                assert(managerDistor != null)
                for (cameraId in managerDistor.cameraIdList) {
                    charsDistor = managerDistor.getCameraCharacteristics(cameraId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            assert(charsDistor != null)


            distortion = charsDistor!!.get(CameraCharacteristics.LENS_RADIAL_DISTORTION)

            val jsonObjectDistortion = JSONObject()

            if (distortion == null) {

                try {
                    jsonObjectDistortion.put("k1", null)
                    jsonObjectDistortion.put("k2", null)
                    jsonObjectDistortion.put("k3", null)
                    jsonObjectDistortion.put("k4", null)
                    jsonObjectDistortion.put("p1", null)
                    jsonObjectDistortion.put("p2", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            } else {

                try {
                    jsonObjectDistortion.put("k1", null)
                    jsonObjectDistortion.put("k2", null)
                    jsonObjectDistortion.put("k3", null)
                    jsonObjectDistortion.put("k4", null)
                    jsonObjectDistortion.put("p1", null)
                    jsonObjectDistortion.put("p2", null)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            }

            val jsonObjectIntrinsics = JSONObject()

            try {
                jsonObjectIntrinsics.put("K", jsonObjectK)
                jsonObjectIntrinsics.put("distortion", jsonObjectDistortion)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------intrinsics Object-----------------------------End*/


            /**********************************/

            /*Start-------------------extrinsics Object-----------------------------Starts*/


            // 1. [Using the Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-rotate)
            //X values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorX = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)


            //[Using the Game Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-gamerot)
            //y values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorY = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)


            //[Using the Geomagnetic Rotation Vector Sensor]
            // (https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-geomrot)
            //z values
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorZ = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

            //Extrinsics Object
            val jsonObjectRotationVec = JSONObject()
            try {
                jsonObjectRotationVec.put("x", mSensorX)
                jsonObjectRotationVec.put("y", mSensorY)
                jsonObjectRotationVec.put("z", mSensorZ)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectExtrinsics = JSONObject()

            try {
                jsonObjectExtrinsics.put("rotation vector component", jsonObjectRotationVec)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            /*End-------------------extrinsics Object-----------------------------End*/


            /**********************************/


            val jsonObjectRoot = JSONObject()

            try {
                jsonObjectRoot.put("points", jsonObjectPoints)
                jsonObjectRoot.put("intrinsics", jsonObjectIntrinsics)
                jsonObjectRoot.put("extrinsics", jsonObjectExtrinsics)
            } catch (e: JSONException) {
                e.printStackTrace()
            }




            restClient.backImageConfig(jsonObjectRoot).enqueue(object : retrofit2.Callback<BackImageConfigRes>{
                override fun onFailure(call: Call<BackImageConfigRes>, t: Throwable) {
                    Toast.makeText(this@ImagesGridActivity_3, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<BackImageConfigRes>, response: Response<BackImageConfigRes>) {
                    if (response.code() == 201) {

                        if (response.isSuccessful())
                            Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()

                        Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 400) {
                        Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or json data could not be read)", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 404) {
                        Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 409) {
                        Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (json data has already loaded)", Toast.LENGTH_SHORT).show()

                    } else if (response.code() == 500) {
                        Toast.makeText(this@ImagesGridActivity_3, "500 Internal Server Error", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this@ImagesGridActivity_3, "False", Toast.LENGTH_SHORT).show()

                    }                          }


            })


        }


    }

    private fun saveImageToFile(file: File?) {
        if (mCameraBitmap != null) {
            var outStream: FileOutputStream? = null
            try {
                outStream = FileOutputStream(file!!)
                if (!mCameraBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
                    Toast.makeText(this@ImagesGridActivity_3, "Unable to save image to file.",
                            Toast.LENGTH_LONG).show()
                } else {
                   /* Toast.makeText(this@ImagesGridActivity_3, "Saved image to: " + file.path,
                            Toast.LENGTH_LONG).show()*/
                }
                outStream.close()
            } catch (e: Exception) {
                Toast.makeText(this@ImagesGridActivity_3, "Unable to save image to file.",
                        Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun openFileForImage(): File? {
        var imageDirectory: File? = null
        val storageState = Environment.getExternalStorageState()
        if (storageState == Environment.MEDIA_MOUNTED) {
            imageDirectory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "a3dyou")
            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                imageDirectory = null
            } else {
                val dateFormat = SimpleDateFormat("yyyy_mm_dd_hh_mm",
                        Locale.getDefault())

                return File(imageDirectory.path +
                        File.separator + "image_" +
                        dateFormat.format(Date()) + ".png")
            }
        }
        return null
    }


    private fun getRealPathFromURI(tempUri: Uri): String? {

        val cursor = contentResolver.query(tempUri, null, null, null, null)
        cursor.moveToFirst();
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        return cursor.getString(idx);

    }

    private fun getImageUri(applicationContext: Context?, photo: Bitmap): Uri {

        val bytes = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        val path: String = MediaStore.Images.Media.insertImage(applicationContext!!.contentResolver,photo,"Title",null)
        return Uri.parse(path);

    }


    fun RotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    class ImagesAdapter : BaseAdapter {
        var foodsList = ArrayList<ImagesModel>()
        var context: Context? = null

        var cameraClickListener: CameraClickListener? = null


        constructor(context: Context, foodsList: ArrayList<ImagesModel>) : super() {
            this.context = context
            this.foodsList = foodsList
        }

        interface CameraClickListener {

            fun onCameraClick(position: Int,imageView: ImageView)

        }

        fun setOnItemClickListener(cameraClickListener: CameraClickListener) {
            this.cameraClickListener = cameraClickListener

        }

        override fun getCount(): Int {
            return foodsList.size
        }

        override fun getItem(position: Int): Any {
            return foodsList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val food = this.foodsList[position]
            var inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var gridView = inflator.inflate(R.layout.grid_item_layout, null)
            gridView.imagePerson.setImageResource(food.image!!)

            /* try {
                 val bitmap = BitmapFactory.decodeStream(context!!.getAssets().open(foodsList[position].image))
                 gridView.imagePerson.setImageBitmap(bitmap)
             } catch (e: Exception) {
                 gridView.imagePerson.setImageResource(R.drawable.person)
             }*/



            // gridView.imagePerson.setImageResource(food.image!!)
            gridView.tvName.text = food.name!!

            gridView.imageCameraGrid.setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {

                    cameraClickListener?.onCameraClick(position,gridView.imagePerson)
                }


            })


            return gridView
        }







    }


}