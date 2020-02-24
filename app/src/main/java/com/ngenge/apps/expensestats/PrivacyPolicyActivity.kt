package com.ngenge.apps.expensestats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        supportActionBar?.title = "Privacy Policy"
    }
}
