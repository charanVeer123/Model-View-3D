package com.dmitrybrant.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import dmax.dialog.SpotsDialog
import android.Manifest
import android.content.pm.PackageManager
import com.dmitrybrant.modelviewer.R
import kotlinx.android.synthetic.main.activity_login_main.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat


/**
 * Created by dharamveer on 28/3/18.
 */

class LoginActivity_1 : AppCompatActivity() {

    var isCheckedRadio: Boolean = true
    val CAMERA_PERMISSION_REQUEST_CODE = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        //Remove notification bar
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)


        setContentView(R.layout.activity_login_main)


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


}
