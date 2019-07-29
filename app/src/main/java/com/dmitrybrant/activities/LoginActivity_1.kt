package com.dmitrybrant.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.dmitrybrant.Service.OnClearFromRecentService
import com.dmitrybrant.Utility
import com.dmitrybrant.Utility.sessionKey
import com.dmitrybrant.modelviewer.R
import com.dmitrybrant.retrofitLibrary.RetrofitLibrary
import com.orhanobut.hawk.Hawk
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_login_main.*
import net.hockeyapp.android.CrashManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity_1 : AppCompatActivity(){

    var isCheckedRadio: Boolean = true
    val CAMERA_PERMISSION_REQUEST_CODE = 3
    //val restClient = RetrofitLibrary.getClient()

    //internal lateinit var utility: Utility

    private fun checkForCrashes() {
        CrashManager.register(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForCrashes()


        ActivityCompat.requestPermissions(this@LoginActivity_1, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)


        val intent = Intent(applicationContext,OnClearFromRecentService::class.java)
        startService(intent)

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        //Remove notification bar
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)


        setContentView(R.layout.activity_login_main)




//        if(Hawk.get<Any>("session_key")==null)
//            createSession1()


        edPassword.setOnEditorActionListener({ textView, i, keyEvent ->
            if (i == R.id.login || i == EditorInfo.IME_NULL) {
                login()
                return@setOnEditorActionListener true
            }
            false
        })

        btnSignIn.setOnClickListener {
            login()
        }


        val MY_PERMISSIONS_REQUEST_CAMERA = 0
        val MY_PERMI_WRITE_EXTERNAL_STORAGE = 0
        // Here, this is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMI_WRITE_EXTERNAL_STORAGE)
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }



    }


    fun createSession1() {

        val restClient = RetrofitLibrary.getClient()

        restClient.createSession().enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                val dialog = MaterialDialog.Builder(this@LoginActivity_1)
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
                    Toast.makeText(applicationContext,"201 Ok",Toast.LENGTH_SHORT).show()
                    if (response.isSuccessful) {

                        //                        sharedPreferencesClass.setSession_key(response.body().toString());
                        sessionKey = response.body()!!.toString()
                        Hawk.put("session_key", sessionKey)
                        Toast.makeText(applicationContext,"sessio key "+ sessionKey ,Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        deleteSession();

    }

    private fun login() {

        if(TextUtils.isEmpty(edEmail.text.toString())){
            edEmail.setError("enter email")
        } else if(TextUtils.isEmpty(edPassword.text.toString())){
            edPassword.setError("enter password")
        }
        else{

            val dialog = SpotsDialog(this,R.style.CustomProgressDialog)
            dialog.show()

            val intent = Intent(this, MainActivity_2::class.java)
            startActivity(intent)
            dialog.dismiss()




        }



    }

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



}
