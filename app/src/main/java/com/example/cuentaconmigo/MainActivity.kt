package com.example.cuentaconmigo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cuentaconmigo.features.main.AppNavGraph
import com.example.cuentaconmigo.ui.theme.CuentaConMigoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CuentaConMigoTheme {
                AppNavGraph()
            }
        }
    }
}