package com.ngenge.apps.expensestats

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.show()
        if (Utils.askForPermissions(this)) {
            Handler().postDelayed({
                startActivity(Intent(this, NavigationActivity::class.java))
                finish()
            }, 2000)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        when (requestCode) {
            Utils.SMS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(Intent(this,NavigationActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "okay", Toast.LENGTH_LONG).show()
                      Utils.askForPermissions(this)
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
