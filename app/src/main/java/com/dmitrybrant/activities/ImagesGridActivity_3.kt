package com.dmitrybrant.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
import com.dmitrybrant.models.ImagesModel
import com.dmitrybrant.modelviewer.MainActivityPlyParser
import com.dmitrybrant.modelviewer.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_captured_images.*
import kotlinx.android.synthetic.main.grid_item_layout.view.*
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
    private val TAKE_PICTURE_REQUEST_B = 100
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

                startImageCapture()

            }

        })

        gridCapture.adapter = adapter

    }


    private fun startImageCapture() {
        // startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_REQUEST_B);
        startActivityForResult(Intent(this@ImagesGridActivity_3, CameraActivity::class.java), TAKE_PICTURE_REQUEST_B)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TAKE_PICTURE_REQUEST_B) {
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




                    imageViewGl!!.setImageBitmap(RotateBitmap(mCameraBitmap!!,90f))



                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        CameraCharacteristics.LENS_INTRINSIC_CALIBRATION
                        CameraCharacteristics.LENS_RADIAL_DISTORTION
                        CameraCharacteristics.LENS_POSE_ROTATION

                    }
                    mCameraBitmap!!.byteCount;


                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                   //  mCapturedImageURI = getImageUri(applicationContext, mCameraBitmap!!)

                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    //val finalFile = File(getRealPathFromURI(mCapturedImageURI!!))

                  //  System.out.println(finalFile)


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
                    Toast.makeText(this@ImagesGridActivity_3, "Saved image to: " + file.path,
                            Toast.LENGTH_LONG).show()
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