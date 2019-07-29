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
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.afollestad.bridge.Bridge
import com.afollestad.materialdialogs.MaterialDialog
import com.dmitrybrant.Utility
import com.dmitrybrant.dialogs.InstructionDialog
import com.dmitrybrant.models.ImagesModel
import com.dmitrybrant.modelviewer.R
import com.dmitrybrant.retrofitLibrary.RetrofitLibrary
import com.orhanobut.hawk.Hawk
import com.sdsmdg.tastytoast.TastyToast
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_captured_images.*
import kotlinx.android.synthetic.main.grid_item_layout.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


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
    private val TAKE_PICTURE_REQUEST_FRONT = 0
    private val TAKE_PICTURE_REQUEST_BACK = 1
    private val TAKE_PICTURE_REQUEST_LEFT = 2
    private val TAKE_PICTURE_REQUEST_RIGHT = 3
    private var mCameraBitmap: Bitmap? = null
    private var txtCreate: TextView? = null
    var myPhotos = BooleanArray(4)

    var hshmapfront = ArrayList<Integer>()
    var hshmapright = ArrayList<Integer>()
    var hshmapback = ArrayList<Integer>()
    var hshmapleft = ArrayList<Integer>()

    var hshmap = ArrayList<Integer>()

    internal var height: String? = null
    internal var gender: String? = null



    fun deleteSession() {

        Hawk.delete("session_key")


        val restClient = RetrofitLibrary.getClient()

        restClient.deleteSession(RetrofitLibrary.GitApiInterface.session_key).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.code() == 200) {


                    Log.d("delete session", "200 ok")

                    Toast.makeText(applicationContext, "200 Ok", Toast.LENGTH_SHORT).show()
                    if (response.isSuccessful) {
                        Utility.sessionKey = null
                    }

                } else if (response.code() == 400) {
                    Log.d("delete session", "400 Bad Request (no 'uuid' query)")
                    Toast.makeText(applicationContext, "400 Bad Request (no 'uuid' query)", Toast.LENGTH_SHORT).show()

                } else if (response.code() == 404) {
                    Log.d("delete session", "404 Not Found")
                    Toast.makeText(applicationContext, "404 Not Found", Toast.LENGTH_SHORT).show()

                } else if (response.code() == 500) {
                    Log.d("delete session", "500 Internal Server Error")
                    Toast.makeText(applicationContext, "500 Internal Server Error", Toast.LENGTH_SHORT).show()

                }


            }

            override fun onFailure(call: Call<String>, t: Throwable) {

                try {
                    Log.d("failure delete session", t.message)
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                } finally {
                    Toast.makeText(applicationContext, "Unknown failure", Toast.LENGTH_SHORT).show()

                }


            }
        })

    }

    fun createSession1() {

        val restClient = RetrofitLibrary.getClient()

        restClient.createSession().enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                val dialog = MaterialDialog.Builder(this@ImagesGridActivity_3)
                        .customView(R.layout.responsedialog, false)
                        .show()

                dialog.setCancelable(true)

//
                val tvrequest = dialog.customView!!.findViewById<View>(R.id.tvrequest) as? TextView
//
                val tvresponse = dialog.customView!!.findViewById<View>(R.id.tvresponse) as? TextView

                val tvParameter = dialog.customView!!.findViewById<View>(R.id.tvrequestparameter) as? TextView

                tvrequest?.setText(response.raw().request().url().toString())

                tvresponse?.setText(response.toString())


                tvParameter?.setText("No parameter")

                if (response.code() == 201) {

                    Log.d("create session", "201 OK (_uuid_ is response body)")
                    uploadImages()


                    Toast.makeText(applicationContext,"201 Ok",Toast.LENGTH_SHORT).show()
                    if (response.isSuccessful) {

                        //                        sharedPreferencesClass.setSession_key(response.body().toString());
                        Utility.sessionKey = response.body()!!.toString()
                        Hawk.put("session_key", Utility.sessionKey)
                        Toast.makeText(applicationContext,"sessio key "+ Utility.sessionKey,Toast.LENGTH_SHORT).show()
                    }

                } else if (response.code() == 500) {
                    Log.d("create session", "500 Internal Server Error")
                    Toast.makeText(applicationContext,"500 Internal Server Error",Toast.LENGTH_SHORT).show()
                    //sharedPreferencesClass.setSession_key("123456");
                    //sessionKey = "123456"
                } else if (response.code() == 503) {
                    Log.d("create session", "503 Service Unavailable (if any session has already created and used)")
                    //                    sharedPreferencesClass.setSession_key("123456");
                    Toast.makeText(applicationContext,"503 Service Unavailable",Toast.LENGTH_SHORT).show()

                    // sessionKey = "123456"
                } else {
                    //                    sharedPreferencesClass.setSession_key("123456");
                    Toast.makeText(applicationContext,"Unknown error",Toast.LENGTH_SHORT).show()

                    // sessionKey = "123456"
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                try {
                    Log.d("failure delete session", t.message)
                    Toast.makeText(applicationContext,t.message,Toast.LENGTH_SHORT).show()
                } finally {
                    Toast.makeText(applicationContext,"Unknown failure",Toast.LENGTH_SHORT).show()

                }
                // sessionKey = "123456"

            }
        })

    }

    private fun uploadImages() {

        genderHeightBack()

        getJsonObjectFront()
        getJsonObjectbackImage()
        getJsonObjectleftImage()
        getJsonObjectrightImage()


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            if (intent.hasExtra("dia")) {
                val instructionDialog = InstructionDialog(this@ImagesGridActivity_3)
                instructionDialog.show()

            }

            if(intent.hasExtra("h"))
            {

                height = intent.getStringExtra("h")
            }

            if(intent.hasExtra("g"))
            {
                gender = intent.getStringExtra("g");
            }

        }

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        //Remove notification bar
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_captured_images)

        txtCreate = findViewById(R.id.txtCreate) as TextView


        val dialog = SpotsDialog(this,R.style.CustomProgressDialog)

        txtCreate!!.setOnClickListener{

          //  dialog.show()

            val mHandler = Handler()
            mHandler.postDelayed(Runnable {


                createSession1()
                //start your activity here
//                deleteSession()
//                finish()
//                val intent = Intent(this, MainActivityPlyParser::class.java)
//                startActivity(intent)
//                dialog.dismiss()






            }, 1000L)



        }




        // load items

        imagesList.add(ImagesModel("Front View", R.drawable.front))
        imagesList.add(ImagesModel("Right View", R.drawable.right))
        imagesList.add(ImagesModel("Back View ", R.drawable.back))
        imagesList.add(ImagesModel("Left View",R.drawable.left))






        adapter = ImagesAdapter(this, imagesList);
        adapter!!.setOnItemClickListener(object : ImagesAdapter.CameraClickListener {
            override fun onCameraClick(position: Int,imageView: ImageView) {

                imageViewGl  = imageView

                if(position==0){
                    startImageCapture(TAKE_PICTURE_REQUEST_FRONT)

                }else if(position==1){
                    startImageCapture(TAKE_PICTURE_REQUEST_RIGHT)
                }
                else if(position==2){
                    startImageCapture(TAKE_PICTURE_REQUEST_BACK)

                }
                else if(position==3){
                    startImageCapture(TAKE_PICTURE_REQUEST_LEFT)

                }

            }

        })

        gridCapture.adapter = adapter



    }



    private fun setIntentData(){



    }



    private fun startImageCapture( requestCode: Int) {


        val intent = Intent(this@ImagesGridActivity_3,AndroidSurfaceviewExample::class.java);


        if(requestCode == TAKE_PICTURE_REQUEST_BACK)
            intent.putExtra("type","back")


        startActivityForResult(intent,requestCode)

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

        if (data != null) {

        }

        //For front image
        if(requestCode == TAKE_PICTURE_REQUEST_FRONT){

            if (resultCode == Activity.RESULT_OK) {

                hshmapfront = data?.getSerializableExtra("hashmap") as ArrayList<Integer>

                imageViewGl!!.rotation= 90F



                if (mCameraBitmap != null && !mCameraBitmap!!.isRecycled()) {
                    // mCameraBitmap!!.recycle();
                    mCameraBitmap = null;
                }

                val extras = data?.extras

                val cameraData = extras!!.getByteArray(CameraActivity.EXTRA_CAMERA_DATA)

                if (true) {

                    var finalFile = data.getStringExtra("file");

                    val  bitMap = BitmapFactory.decodeFile(finalFile)


                    val requestFile = RequestBody.create(MediaType.parse("image/*"), finalFile)


                    //Api for front image
                    restClient.uploadfrontImage(requestFile,RetrofitLibrary.GitApiInterface.session_key).enqueue(object : retrofit2.Callback<String> {

                        override fun onResponse(call: Call<String>, response: Response<String>) {

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


                        override fun onFailure(call: Call<String>?, t: Throwable?) {
                            Toast.makeText(this@ImagesGridActivity_3, t.toString(), Toast.LENGTH_SHORT).show()
                        }

                    })


//                    genderHeightFront()
//                    getJsonObjectFront()




                    imageViewGl!!.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageViewGl!!.setImageBitmap(bitMap)



//                    mCameraBitmap!!.byteCount;


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
                imageViewGl!!.rotation= 90F

                if (mCameraBitmap != null && !mCameraBitmap!!.isRecycled()) {
                    // mCameraBitmap!!.recycle();
                    mCameraBitmap = null;
                }

//                val extras = data?.extras
//                val cameraData = extras!!.getByteArray(CameraActivity.EXTRA_CAMERA_DATA)

                if (true) {
                    hshmapback = data?.getSerializableExtra("hashmap") as ArrayList<Integer>

                    var finalFile = data?.getStringExtra("file");

                    val  bitMap = BitmapFactory.decodeFile(finalFile)
//                    mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.size)
//
//                    // val photo: Bitmap// this is your image.
//                    val stream = ByteArrayOutputStream()
//                    mCameraBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
//
//
//
//                    //creating request body for file
//
//                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
//                    mCapturedImageURI = getImageUri(applicationContext, mCameraBitmap!!)
//
//                    // CALL THIS METHOD TO GET THE ACTUAL PATH
//                    val finalFile = File(getRealPathFromURI(mCapturedImageURI!!))


                    // val requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(mCapturedImageURI)!!), finalFile)

                    val requestFile = RequestBody.create(MediaType.parse("image/*"), finalFile)


                    //Api for back image
                    restClient.uploadbackImage(requestFile,RetrofitLibrary.GitApiInterface.session_key).enqueue(object : retrofit2.Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
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

                        override fun onFailure(call: Call<String>?, t: Throwable?) {
                            Toast.makeText(this@ImagesGridActivity_3, t.toString(), Toast.LENGTH_SHORT).show()
                        }

                    })


//                    genderHeightBack()
//                    getJsonObjectbackImage()


                    imageViewGl!!.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageViewGl!!.setImageBitmap(bitMap)

//                    myPhotos[1]=true;
//                    mCameraBitmap!!.byteCount;





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


        //For left image
        else if (requestCode == TAKE_PICTURE_REQUEST_LEFT) {

            if (resultCode == Activity.RESULT_OK) {
                // Recycle the previous bitmap.
                imageViewGl!!.rotation= 90F


                if (mCameraBitmap != null && !mCameraBitmap!!.isRecycled()) {
                    // mCameraBitmap!!.recycle();
                    mCameraBitmap = null;
                }

//
//                val extras = data?.extras
//                val cameraData = extras!!.getByteArray(CameraActivity.EXTRA_CAMERA_DATA)

                if (true) {

//                    mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.size)
                    hshmapleft = data?.getSerializableExtra("hashmap") as ArrayList<Integer>
                    var finalFile = data?.getStringExtra("file");

                    val  bitMap = BitmapFactory.decodeFile(finalFile)
//                    // val photo: Bitmap// this is your image.
//                    val stream = ByteArrayOutputStream()
//                    mCameraBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
//
//
//
//                    //creating request body for file
//
//                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
//                    mCapturedImageURI = getImageUri(applicationContext, mCameraBitmap!!)

                    // CALL THIS METHOD TO GET THE ACTUAL PATH
//                    val finalFile = File(getRealPathFromURI(mCapturedImageURI!!))


                    // val requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(mCapturedImageURI)!!), finalFile)
                    // val requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(mCapturedImageURI)!!), finalFile)
                    val requestFile = RequestBody.create(MediaType.parse("image/*"), finalFile)


                    //java.io.EOFException: End of input at line 1 column 1

                    //Api for left image
                    restClient.uploadleftImage(requestFile,RetrofitLibrary.GitApiInterface.session_key).enqueue(object : retrofit2.Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {

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

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Toast.makeText(this@ImagesGridActivity_3,t.toString(),Toast.LENGTH_SHORT).show()

                        }
                    })


//                    genderHeightLeft()
//                    getJsonObjectleftImage()

                    imageViewGl!!.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageViewGl!!.setImageBitmap(bitMap)


//                    myPhotos[2]=true;

//                    mCameraBitmap!!.byteCount;


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
                imageViewGl!!.rotation= 90F
                if (mCameraBitmap != null && !mCameraBitmap!!.isRecycled()) {
                    // mCameraBitmap!!.recycle();
                    mCameraBitmap = null;
                }
//
//                val extras = data?.extras
//                val cameraData = extras!!.getByteArray(CameraActivity.EXTRA_CAMERA_DATA)


                if (true) {
                    hshmapright = data?.getSerializableExtra("hashmap") as ArrayList<Integer>
                    var finalFile = data?.getStringExtra("file");

                    val  bitMap = BitmapFactory.decodeFile(finalFile)

//                    mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.size)
//
//                    // val photo: Bitmap// this is your image.
//                    val stream = ByteArrayOutputStream()
//                    mCameraBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
//
//
//
//                    //creating request body for file
//
//                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
//                    mCapturedImageURI = getImageUri(applicationContext, mCameraBitmap!!)
//
//                    // CALL THIS METHOD TO GET THE ACTUAL PATH
//                    val finalFile = File(getRealPathFromURI(mCapturedImageURI!!))
//
//
//                    //val requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(mCapturedImageURI)!!), finalFile)
//
//
                    val requestFile = RequestBody.create(MediaType.parse("image/*"), finalFile)


                    //Api for right image
                    restClient.uploadrightImage(requestFile,RetrofitLibrary.GitApiInterface.session_key).enqueue(object : retrofit2.Callback<String> {

                        override fun onResponse(call: Call<String>, response: Response<String>) {
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

                        override fun onFailure(call: Call<String>?, t: Throwable?) {
                            Toast.makeText(this@ImagesGridActivity_3, t.toString(), Toast.LENGTH_SHORT).show()
                        }


                    })

//                    genderHeightRight()
//                    getJsonObjectrightImage()

                    imageViewGl!!.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageViewGl!!.setImageBitmap(bitMap)


//                    myPhotos[3]=true;


//                    mCameraBitmap!!.byteCount;



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

    private fun genderHeightRight() {
        genderHeightFront()

//        restClient.genderHeight(Utility.sessionKey,gender, height.toString()).enqueue(object : retrofit2.Callback<String>{
//
//
//
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//
//                val dialog = MaterialDialog.Builder(this@ImagesGridActivity_3)
//                        .customView(R.layout.responsedialog, false)
//                        .show()
//
//                dialog.setCancelable(true)
//
////
//                val tvrequest = dialog.customView!!.findViewById<View>(R.id.tvrequest) as? TextView
////
//                val tvresponse = dialog.customView!!.findViewById<View>(R.id.tvresponse) as? TextView
//
//                val tvParameter = dialog.customView!!.findViewById<View>(R.id.tvrequestparameter) as? TextView
//
//                tvrequest?.setText(response.raw().request().url().toString())
//
//                tvresponse?.setText(response.toString())
//
//
//                tvParameter?.setText("No request parameter only header")
//
//
//
//
//
//                if (response.code() == 201) {
//
//                    Log.d("genderHeight", "201 OK (_uuid_ is response body)")
//                    Toast.makeText(applicationContext,"201 Ok",Toast.LENGTH_SHORT).show()
//                    if (response.isSuccessful) {
//
//
//                        Toast.makeText(applicationContext,"sessio key "+ Utility.sessionKey,Toast.LENGTH_SHORT).show()
//                    }
//
//                } else if (response.code() == 400) {
//                    Log.d("genderHeight", "500 Internal Server Error")
//                    Toast.makeText(applicationContext,"400 Bad Request (no 'uuid' or 'gender' or 'height' query or query parameter is wrong)",Toast.LENGTH_SHORT).show()
//
//                } else if (response.code() == 404) {
//                    Log.d("genderHeight", "404 Not Found")
//
//                    Toast.makeText(applicationContext,"404 Not Found",Toast.LENGTH_SHORT).show()
//
//
//                }
//                else if (response.code() == 409) {
//                    Log.d("genderHeight", "409 Conflict (json data has already loaded)")
//
//                    Toast.makeText(applicationContext,"409 Conflict (json data has already loaded)",Toast.LENGTH_SHORT).show()
//
//
//                }
//                else if (response.code() == 500) {
//                    Log.d("genderHeight", "500 Internal Server Error")
//
//                    Toast.makeText(applicationContext,"500 Internal Server Error",Toast.LENGTH_SHORT).show()
//
//
//                }
//
//
//                else {
//                    Toast.makeText(applicationContext,"Unknown error",Toast.LENGTH_SHORT).show()
//
//                }
//
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//
//                try {
//                    Log.d("failure ", t.message)
//                    Toast.makeText(applicationContext,t.message,Toast.LENGTH_SHORT).show()
//                } finally {
//                    Toast.makeText(applicationContext,"Unknown failure",Toast.LENGTH_SHORT).show()
//
//                }
//
//            }
//
//
//
//        })
    }

    private fun genderHeightLeft() {

        genderHeightFront()
//        restClient.genderHeight(Utility.sessionKey,gender,height.toString()).enqueue(object : retrofit2.Callback<String>{
//            override fun onFailure(call: Call<String>, t: Throwable) {
//
//                try {
//                    Log.d("failure ", t.message)
//                    Toast.makeText(applicationContext,t.message,Toast.LENGTH_SHORT).show()
//                } finally {
//                    Toast.makeText(applicationContext,"Unknown failure",Toast.LENGTH_SHORT).show()
//
//                }
//
//            }
//
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                val dialog = MaterialDialog.Builder(this@ImagesGridActivity_3)
//                        .customView(R.layout.responsedialog, false)
//                        .show()
//
//                dialog.setCancelable(true)
//
////
//                val tvrequest = dialog.customView!!.findViewById<View>(R.id.tvrequest) as? TextView
////
//                val tvresponse = dialog.customView!!.findViewById<View>(R.id.tvresponse) as? TextView
//
//                val tvParameter = dialog.customView!!.findViewById<View>(R.id.tvrequestparameter) as? TextView
//
//                tvrequest?.setText(response.raw().request().url().toString())
//
//                tvresponse?.setText(response.toString())
//
//
//                tvParameter?.setText("No request parameter only header")
//
//                if (response.code() == 201) {
//
//                    Log.d("genderHeight", "201 OK (_uuid_ is response body)")
//                    Toast.makeText(applicationContext,"201 Ok",Toast.LENGTH_SHORT).show()
//                    if (response.isSuccessful) {
//
//
//                        Toast.makeText(applicationContext,"sessio key "+ Utility.sessionKey,Toast.LENGTH_SHORT).show()
//                    }
//
//                } else if (response.code() == 400) {
//                    Log.d("genderHeight", "500 Internal Server Error")
//                    Toast.makeText(applicationContext,"400 Bad Request (no 'uuid' or 'gender' or 'height' query or query parameter is wrong)",Toast.LENGTH_SHORT).show()
//
//                } else if (response.code() == 404) {
//                    Log.d("genderHeight", "404 Not Found")
//
//                    Toast.makeText(applicationContext,"404 Not Found",Toast.LENGTH_SHORT).show()
//
//
//                }
//                else if (response.code() == 409) {
//                    Log.d("genderHeight", "409 Conflict (json data has already loaded)")
//
//                    Toast.makeText(applicationContext,"409 Conflict (json data has already loaded)",Toast.LENGTH_SHORT).show()
//
//
//                }
//                else if (response.code() == 500) {
//                    Log.d("genderHeight", "500 Internal Server Error")
//
//                    Toast.makeText(applicationContext,"500 Internal Server Error",Toast.LENGTH_SHORT).show()
//
//
//                }
//
//
//                else {
//                    Toast.makeText(applicationContext,"Unknown error",Toast.LENGTH_SHORT).show()
//
//                }
//            }
//
//        })
    }

    private fun genderHeightBack() {
        genderHeightFront()

//        restClient.genderHeight(Utility.sessionKey,gender,height.toString()).enqueue(object : retrofit2.Callback<String>{
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                try {
//                    Log.d("failure ", t.message)
//                    Toast.makeText(applicationContext,t.message,Toast.LENGTH_SHORT).show()
//                } finally {
//                    Toast.makeText(applicationContext,"Unknown failure",Toast.LENGTH_SHORT).show()
//
//                }
//
//            }
//
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                val dialog = MaterialDialog.Builder(this@ImagesGridActivity_3)
//                        .customView(R.layout.responsedialog, false)
//                        .show()
//
//                dialog.setCancelable(true)
//
////
//                val tvrequest = dialog.customView!!.findViewById<View>(R.id.tvrequest) as? TextView
////
//                val tvresponse = dialog.customView!!.findViewById<View>(R.id.tvresponse) as? TextView
//
//                val tvParameter = dialog.customView!!.findViewById<View>(R.id.tvrequestparameter) as? TextView
//
//                tvrequest?.setText(response.raw().request().url().toString())
//
//                tvParameter?.setText(response.toString())
//
//
//                tvresponse?.setText("No request parameter only header")
//                if (response.code() == 201) {
//
//                    Log.d("genderHeight", "201 OK (_uuid_ is response body)")
//                    Toast.makeText(applicationContext,"201 Ok",Toast.LENGTH_SHORT).show()
//                    if (response.isSuccessful) {
//
//
//                        Toast.makeText(applicationContext,"sessio key "+ Utility.sessionKey,Toast.LENGTH_SHORT).show()
//                    }
//
//                } else if (response.code() == 400) {
//                    Log.d("genderHeight", "500 Internal Server Error")
//                    Toast.makeText(applicationContext,"400 Bad Request (no 'uuid' or 'gender' or 'height' query or query parameter is wrong)",Toast.LENGTH_SHORT).show()
//
//                } else if (response.code() == 404) {
//                    Log.d("genderHeight", "404 Not Found")
//
//                    Toast.makeText(applicationContext,"404 Not Found",Toast.LENGTH_SHORT).show()
//
//
//                }
//                else if (response.code() == 409) {
//                    Log.d("genderHeight", "409 Conflict (json data has already loaded)")
//
//                    Toast.makeText(applicationContext,"409 Conflict (json data has already loaded)",Toast.LENGTH_SHORT).show()
//
//
//                }
//                else if (response.code() == 500) {
//                    Log.d("genderHeight", "500 Internal Server Error")
//
//                    Toast.makeText(applicationContext,"500 Internal Server Error",Toast.LENGTH_SHORT).show()
//
//
//                }
//
//
//                else {
//                    Toast.makeText(applicationContext,"Unknown error",Toast.LENGTH_SHORT).show()
//
//                }
//            }
//
//        })
    }

    private fun genderHeightFront() {
        Thread(Runnable {

            val postContent = ""
            val request = Bridge
                    .post("https://a3dyou.com:9000/configuration?uuid=d687c943-08cc-4093-a810-658fcb70c9e6&gender=male&height=250")
                    .body(postContent)
                    .request()

            request.response().asString()


        }).start()

//        restClient.genderHeight(Utility.sessionKey,gender,height .toString()).enqueue(object : retrofit2.Callback<String>{
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                try {
//                    Log.d("failure ", t.message)
//                    Toast.makeText(applicationContext,t.message,Toast.LENGTH_SHORT).show()
//                } finally {
//                    Toast.makeText(applicationContext,"Unknown failure",Toast.LENGTH_SHORT).show()
//
//                }
//            }
//
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//
//
//                val dialog = MaterialDialog.Builder(this@ImagesGridActivity_3)
//                        .customView(R.layout.responsedialog, false)
//                        .show()
//
//                dialog.setCancelable(true)
//
////
//                val tvrequest = dialog.customView!!.findViewById<View>(R.id.tvrequest) as? TextView
////
//                val tvresponse = dialog.customView!!.findViewById<View>(R.id.tvresponse) as? TextView
//
//                val tvParameter = dialog.customView!!.findViewById<View>(R.id.tvrequestparameter) as? TextView
//
//                tvrequest?.setText(response.raw().request().url().toString())
//
//                tvresponse?.setText(response.toString())
//
//
//                tvParameter?.setText("No request parameter only header")
//
//                if (response.code() == 201) {
//
//                    Log.d("genderHeight", "201 OK (_uuid_ is response body)")
//                    Toast.makeText(applicationContext,"201 Ok",Toast.LENGTH_SHORT).show()
//                    if (response.isSuccessful) {
//
//
//                        Toast.makeText(applicationContext,"sessio key "+ Utility.sessionKey,Toast.LENGTH_SHORT).show()
//                    }
//
//                } else if (response.code() == 400) {
//                    Log.d("genderHeight", "500 Internal Server Error")
//                    Toast.makeText(applicationContext,"400 Bad Request (no 'uuid' or 'gender' or 'height' query or query parameter is wrong)",Toast.LENGTH_SHORT).show()
//
//                } else if (response.code() == 404) {
//                    Log.d("genderHeight", "404 Not Found")
//
//                    Toast.makeText(applicationContext,"404 Not Found",Toast.LENGTH_SHORT).show()
//
//
//                }
//                else if (response.code() == 409) {
//                    Log.d("genderHeight", "409 Conflict (json data has already loaded)")
//
//                    Toast.makeText(applicationContext,"409 Conflict (json data has already loaded)",Toast.LENGTH_SHORT).show()
//
//
//                }
//                else if (response.code() == 500) {
//                    Log.d("genderHeight", "500 Internal Server Error")
//
//                    Toast.makeText(applicationContext,"500 Internal Server Error",Toast.LENGTH_SHORT).show()
//
//                }
//
//                else {
//                    Toast.makeText(applicationContext,"Unknown error",Toast.LENGTH_SHORT).show()
//                }
//            }
//
//        })

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
                if(hshmap.size>0)
                {
                    jsonObjectTop.put("x", hshmap[0])
                    jsonObjectTop.put("y", hshmap[1])
                }
                else
                {
                    jsonObjectTop.put("x", 0)
                    jsonObjectTop.put("y", 0)
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectBottom = JSONObject()
            try {
                if(hshmap.size>0)
                {
                    jsonObjectBottom.put("x", hshmap[2])
                    jsonObjectBottom.put("y", hshmap[3])
                }
                else
                {
                    jsonObjectBottom.put("x", 0)
                    jsonObjectBottom.put("y", 0)
                }

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


            Thread(Runnable {
                val request = Bridge
                        .post("https://a3dyou.com:9000/configuration/front?uuid="+Hawk.get("session_key"))
                        .body("{\n" +
                                "    \"points\" : {\n" +
                                "        \"top\" : {\n" +
                                "            \"x\" :"+hshmap[0]+",\n" +
                                "            \"y\" : "+hshmap[1]+",\n" +
                                "        },\n" +
                                "        \"bottom\" : {\n" +
                                "            \"x\" :"+ hshmap[2]+",\n" +
                                "            \"y\" : "+hshmap[3]+"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"intrinsics\" : {\n" +
                                "        \"K\" : { \n" +
                                "            \"fx\" :"+ 0+",\n" +
                                "            \"fy\" : 0,\n" +
                                "            \"cx\" : 0,\n" +
                                "            \"cy\" : 0,\n" +
                                "            \"skew\" : 0\n" +
                                "        },\n" +
                                "        \"distortion\" : {\n" +
                                "            \"k1\" : 0,\n" +
                                "            \"k2\" : 0,\n" +
                                "            \"k3\" : 0,\n" +
                                "            \"k4\" :0,\n" +
                                "            \"p1\" : 0,\n" +
                                "            \"p2\" : 0\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"extrinsics\" : {\n" +
                                "        \"rotation vector component\" : {\n" +
                                "            \"x\" : 0,\n" +
                                "            \"y\" : 0,\n" +
                                "            \"z\" : 0\n" +
                                "        }\n" +
                                "    }\n" +
                                "}")
                        .request()

                request.response().asString();

                request.response().code()

            }).start()







            TastyToast.makeText(applicationContext,"request is ->>>>"+jsonObjectRoot.toString(),TastyToast.LENGTH_LONG,TastyToast.INFO).show()


//            restClient.frontImageConfig(jsonObjectRoot,RetrofitLibrary.GitApiInterface.session_key).enqueue(object : retrofit2.Callback<String>{
//                override fun onResponse(call: Call<String>, response: Response<String>) {
//
//                    val dialog = MaterialDialog.Builder(this@ImagesGridActivity_3)
//                            .customView(R.layout.responsedialog, false)
//                            .show()
//
//                    dialog.setCancelable(true)
//
////
//                    val tvrequest = dialog.customView!!.findViewById<View>(R.id.tvrequest) as? TextView
////
//                    val tvresponse = dialog.customView!!.findViewById<View>(R.id.tvresponse) as? TextView
//
//                    val tvParameter = dialog.customView!!.findViewById<View>(R.id.tvrequestparameter) as? TextView
//
//                    tvrequest?.setText(response.raw().request().url().toString())
//
//                    tvresponse?.setText(response.toString())
//
//
//                    tvParameter?.setText(jsonObjectRoot.toString())
//
//
//                    if (response.code() == 201) {
//
//                        if (response.isSuccessful())
//                            Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()
//
//                        Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 400) {
//                        Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or json data could not be read)", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 404) {
//                        Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 409) {
//                        Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (json data has already loaded)", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 500) {
//                        Toast.makeText(this@ImagesGridActivity_3, "500 Internal Server Error", Toast.LENGTH_SHORT).show()
//
//                    } else {
//                        Toast.makeText(this@ImagesGridActivity_3, "False", Toast.LENGTH_SHORT).show()
//
//                    }
//
//                }
//
//                override fun onFailure(call: Call<String>, t: Throwable) {
//                    Log.d("failure ", t.message)
//
//                    Toast.makeText(this@ImagesGridActivity_3, t.message.toString(), Toast.LENGTH_SHORT).show()
//
//                }
//
//            })
        }


    }

    private fun getJsonObjectleftImage() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            /*Start-------------------Points Object-----------------------------Starts*/

            val jsonObjectTop = JSONObject()
            try {
                if(hshmap.size>0)
                {
                    jsonObjectTop.put("x", hshmap[0])
                    jsonObjectTop.put("y", hshmap[1])
                }
                else
                {
                    jsonObjectTop.put("x", 0)
                    jsonObjectTop.put("y", 0)
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectBottom = JSONObject()
            try {
                if(hshmap.size>0)
                {
                    jsonObjectBottom.put("x", hshmap[2])
                    jsonObjectBottom.put("y", hshmap[3])
                }
                else
                {
                    jsonObjectBottom.put("x", 0)
                    jsonObjectBottom.put("y", 0)
                }

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

            val lensIntrinsicCalibration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CameraCharacteristics.LENS_INTRINSIC_CALIBRATION
            } else {
                TODO("VERSION.SDK_INT < M")
            }

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
            val facing = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    chars!!.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION)
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP")
            }

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

            TastyToast.makeText(applicationContext,"request is ->>>>"+jsonObjectRoot.toString(),TastyToast.LENGTH_LONG,TastyToast.INFO).show()
            Thread(Runnable {
                val request = Bridge
                        .post("https://a3dyou.com:9000/configuration/left?uuid="+Hawk.get("session_key"))
                        .body("{\n" +
                                "    \"points\" : {\n" +
                                "        \"top\" : {\n" +
                                "            \"x\" :"+hshmap[0]+",\n" +
                                "            \"y\" : "+hshmap[1]+",\n" +
                                "        },\n" +
                                "        \"bottom\" : {\n" +
                                "            \"x\" :"+ hshmap[2]+",\n" +
                                "            \"y\" : "+hshmap[3]+"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"intrinsics\" : {\n" +
                                "        \"K\" : { \n" +
                                "            \"fx\" :"+ 0+",\n" +
                                "            \"fy\" : 0,\n" +
                                "            \"cx\" : 0,\n" +
                                "            \"cy\" : 0,\n" +
                                "            \"skew\" : 0\n" +
                                "        },\n" +
                                "        \"distortion\" : {\n" +
                                "            \"k1\" : 0,\n" +
                                "            \"k2\" : 0,\n" +
                                "            \"k3\" : 0,\n" +
                                "            \"k4\" :0,\n" +
                                "            \"p1\" : 0,\n" +
                                "            \"p2\" : 0\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"extrinsics\" : {\n" +
                                "        \"rotation vector component\" : {\n" +
                                "            \"x\" : 0,\n" +
                                "            \"y\" : 0,\n" +
                                "            \"z\" : 0\n" +
                                "        }\n" +
                                "    }\n" +
                                "}")
                        .request()

                request.response().asString();

                request.response().code()

            }).start()


//            restClient.leftImageConfig(jsonObjectRoot,RetrofitLibrary.GitApiInterface.session_key).enqueue(object : retrofit2.Callback<String>{
//                override fun onFailure(call: Call<String>, t: Throwable) {
//                    Toast.makeText(this@ImagesGridActivity_3, t.message, Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onResponse(call: Call<String>, response: Response<String>) {
//
//                    val dialog = MaterialDialog.Builder(this@ImagesGridActivity_3)
//                            .customView(R.layout.responsedialog, false)
//                            .show()
//
//                    dialog.setCancelable(true)
//
////
//                    val tvrequest = dialog.customView!!.findViewById<View>(R.id.tvrequest) as? TextView
////
//                    val tvresponse = dialog.customView!!.findViewById<View>(R.id.tvresponse) as? TextView
//
//                    val tvParameter = dialog.customView!!.findViewById<View>(R.id.tvrequestparameter) as? TextView
//
//                    tvrequest?.setText(response.raw().request().url().toString())
//
//                    tvresponse?.setText(response.toString())
//
//
//                    tvParameter?.setText(jsonObjectRoot.toString())
//                    if (response.code() == 201) {
//
//                        if (response.isSuccessful())
//                            Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()
//
//                        Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 400) {
//                        Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or json data could not be read)", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 404) {
//                        Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 409) {
//                        Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (json data has already loaded)", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 500) {
//                        Toast.makeText(this@ImagesGridActivity_3, "500 Internal Server Error", Toast.LENGTH_SHORT).show()
//
//                    } else {
//                        Toast.makeText(this@ImagesGridActivity_3, "False", Toast.LENGTH_SHORT).show()
//
//                    }
//                }
//
//            })


        }

    }

    private fun getJsonObjectrightImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            /*Start-------------------Points Object-----------------------------Starts*/

            val jsonObjectTop = JSONObject()
            try {
                if(hshmap.size>0)
                {
                    jsonObjectTop.put("x", hshmap[0])
                    jsonObjectTop.put("y", hshmap[1])
                }
                else
                {
                    jsonObjectTop.put("x", 0)
                    jsonObjectTop.put("y", 0)
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectBottom = JSONObject()
            try {
                if(hshmap.size>0)
                {
                    jsonObjectBottom.put("x", hshmap[2])
                    jsonObjectBottom.put("y", hshmap[3])
                }
                else
                {
                    jsonObjectBottom.put("x", 0)
                    jsonObjectBottom.put("y", 0)
                }

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


            TastyToast.makeText(applicationContext,"request is ->>>>"+jsonObjectRoot.toString(),TastyToast.LENGTH_LONG,TastyToast.INFO).show()
            Thread(Runnable {
                val request = Bridge
                        .post("https://a3dyou.com:9000/configuration/right?uuid="+Hawk.get("session_key"))
                        .body("{\n" +
                                "    \"points\" : {\n" +
                                "        \"top\" : {\n" +
                                "            \"x\" :"+hshmap[0]+",\n" +
                                "            \"y\" : "+hshmap[1]+",\n" +
                                "        },\n" +
                                "        \"bottom\" : {\n" +
                                "            \"x\" :"+ hshmap[2]+",\n" +
                                "            \"y\" : "+hshmap[3]+"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"intrinsics\" : {\n" +
                                "        \"K\" : { \n" +
                                "            \"fx\" :"+ 0+",\n" +
                                "            \"fy\" : 0,\n" +
                                "            \"cx\" : 0,\n" +
                                "            \"cy\" : 0,\n" +
                                "            \"skew\" : 0\n" +
                                "        },\n" +
                                "        \"distortion\" : {\n" +
                                "            \"k1\" : 0,\n" +
                                "            \"k2\" : 0,\n" +
                                "            \"k3\" : 0,\n" +
                                "            \"k4\" :0,\n" +
                                "            \"p1\" : 0,\n" +
                                "            \"p2\" : 0\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"extrinsics\" : {\n" +
                                "        \"rotation vector component\" : {\n" +
                                "            \"x\" : 0,\n" +
                                "            \"y\" : 0,\n" +
                                "            \"z\" : 0\n" +
                                "        }\n" +
                                "    }\n" +
                                "}")
                        .request()

                request.response().asString();

                request.response().code()

            }).start()

//            restClient.rightImageConfig(jsonObjectRoot,RetrofitLibrary.GitApiInterface.session_key).enqueue(object : retrofit2.Callback<String>{
//                override fun onFailure(call: Call<String>, t: Throwable) {
//                    Toast.makeText(this@ImagesGridActivity_3, t.message, Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onResponse(call: Call<String>, response: Response<String>) {
//
//                    val dialog = MaterialDialog.Builder(this@ImagesGridActivity_3)
//                            .customView(R.layout.responsedialog, false)
//                            .show()
//
//                    dialog.setCancelable(true)
//
////
//                    val tvrequest = dialog.customView!!.findViewById<View>(R.id.tvrequest) as? TextView
////
//                    val tvresponse = dialog.customView!!.findViewById<View>(R.id.tvresponse) as? TextView
//
//                    val tvParameter = dialog.customView!!.findViewById<View>(R.id.tvrequestparameter) as? TextView
//
//                    tvrequest?.setText(response.raw().request().url().toString())
//
//                    tvresponse?.setText(response.toString())
//
//
//                    tvParameter?.setText(jsonObjectRoot.toString())
//                    if (response.code() == 201) {
//
//                        if (response.isSuccessful())
//                            Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()
//
//                        Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 400) {
//                        Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or json data could not be read)", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 404) {
//                        Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 409) {
//                        Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (json data has already loaded)", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 500) {
//                        Toast.makeText(this@ImagesGridActivity_3, "500 Internal Server Error", Toast.LENGTH_SHORT).show()
//
//                    } else {
//                        Toast.makeText(this@ImagesGridActivity_3, "False", Toast.LENGTH_SHORT).show()
//
//                    }
//                }
//
//
//            })


        }


    }

    private fun getJsonObjectbackImage()   {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            /*Start-------------------Points Object-----------------------------Starts*/

            val jsonObjectTop = JSONObject()
            try {
                if(hshmap.size>0)
                {
                    jsonObjectTop.put("x", hshmap[0])
                    jsonObjectTop.put("y", hshmap[1])
                }
                else
                {
                    jsonObjectTop.put("x", 0)
                    jsonObjectTop.put("y", 0)
                }


            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val jsonObjectBottom = JSONObject()
            try {if(hshmap.size>0)
            {
                jsonObjectBottom.put("x", hshmap[2])
                jsonObjectBottom.put("y", hshmap[3])
            }
            else
            {
                jsonObjectBottom.put("x", 0)
                jsonObjectBottom.put("y", 0)
            }

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


            TastyToast.makeText(applicationContext,"request is ->>>>"+jsonObjectRoot.toString(),TastyToast.LENGTH_LONG,TastyToast.INFO).show()


            Thread(Runnable {
                val request = Bridge
                        .post("https://a3dyou.com:9000/configuration/back?uuid="+Hawk.get("session_key"))
                        .body("{\n" +
                                "    \"points\" : {\n" +
                                "        \"top\" : {\n" +
                                "            \"x\" :"+hshmap[0]+",\n" +
                                "            \"y\" : "+hshmap[1]+",\n" +
                                "        },\n" +
                                "        \"bottom\" : {\n" +
                                "            \"x\" :"+ hshmap[2]+",\n" +
                                "            \"y\" : "+hshmap[3]+"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"intrinsics\" : {\n" +
                                "        \"K\" : { \n" +
                                "            \"fx\" :"+ 0+",\n" +
                                "            \"fy\" :"+ 0+",\n" +
                                "            \"cx\" :"+ 0+",\n" +
                                "            \"cy\" :"+ 0+",\n" +
                                "            \"skew\" :"+ 0+"\n" +
                                "        },\n" +
                                "        \"distortion\" : {\n" +
                                "            \"k1\" : 0,\n" +
                                "            \"k2\" : 0,\n" +
                                "            \"k3\" : 0,\n" +
                                "            \"k4\" :0,\n" +
                                "            \"p1\" : 0,\n" +
                                "            \"p2\" : 0\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"extrinsics\" : {\n" +
                                "        \"rotation vector component\" : {\n" +
                                "            \"x\" : 0,\n" +
                                "            \"y\" : 0,\n" +
                                "            \"z\" : 0\n" +
                                "        }\n" +
                                "    }\n" +
                                "}")
                        .request()

                request.response().asString();

                request.response().code()

            }).start()

//            restClient.backImageConfig(jsonObjectRoot,RetrofitLibrary.GitApiInterface.session_key).enqueue(object : retrofit2.Callback<String>{
//                override fun onFailure(call: Call<String>, t: Throwable) {
//                    Toast.makeText(this@ImagesGridActivity_3, t.message, Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onResponse(call: Call<String>, response: Response<String>) {
//
//                    val dialog = MaterialDialog.Builder(this@ImagesGridActivity_3)
//                            .customView(R.layout.responsedialog, false)
//                            .show()
//
//                    dialog.setCancelable(true)
//
////
//                    val tvrequest = dialog.customView!!.findViewById<View>(R.id.tvrequest) as? TextView
////
//                    val tvresponse = dialog.customView!!.findViewById<View>(R.id.tvresponse) as? TextView
//
//                    val tvParameter = dialog.customView!!.findViewById<View>(R.id.tvrequestparameter) as? TextView
//
//                    tvrequest?.setText(response.raw().request().url().toString())
//
//                    tvresponse?.setText(response.toString())
//
//
//                    tvParameter?.setText(jsonObjectRoot.toString())
//                    if (response.code() == 201) {
//
//                        if (response.isSuccessful())
//                            Toast.makeText(this@ImagesGridActivity_3, "Success", Toast.LENGTH_SHORT).show()
//
//                        Toast.makeText(this@ImagesGridActivity_3, "OK", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 400) {
//                        Toast.makeText(this@ImagesGridActivity_3, "Bad Request (no 'uuid' query or json data could not be read)", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 404) {
//                        Toast.makeText(this@ImagesGridActivity_3, "404 Not Found", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 409) {
//                        Toast.makeText(this@ImagesGridActivity_3, "409 Conflict (json data has already loaded)", Toast.LENGTH_SHORT).show()
//
//                    } else if (response.code() == 500) {
//                        Toast.makeText(this@ImagesGridActivity_3, "500 Internal Server Error", Toast.LENGTH_SHORT).show()
//
//                    } else {
//                        Toast.makeText(this@ImagesGridActivity_3, "False", Toast.LENGTH_SHORT).show()
//
//                    }                          }
//
//
//            })


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

    override fun onResume() {
        super.onResume()

    }

}