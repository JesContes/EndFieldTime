package com.endfield.time

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.endfield.time.ui.nav.AppNav
import com.endfield.time.ui.theme.EndFieldTimeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EndFieldTimeTheme {
                AppNav()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (application as EndFieldApp).container.onAppResumed()
    }
}
