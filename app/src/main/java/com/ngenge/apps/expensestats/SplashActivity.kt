package com.ngenge.apps.expensestats

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

    private val SMS_REQUEST_CODE: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_DENIED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {

                Toast.makeText(this,"Permissions are required to read SMS for Ecobank", Toast.LENGTH_LONG).show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS),SMS_REQUEST_CODE)
            }
        } else {

            Handler().postDelayed({
                startActivity(Intent(this, NavigationActivity::class.java))
                finish()
            }, 2000)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when(requestCode) {
            SMS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startActivity(Intent(this,NavigationActivity::class.java))
                    finish()
                } else {

                    Toast.makeText(this,"Permission to read SMS denied. Closing app",Toast.LENGTH_LONG).show()
                    finish()

                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
