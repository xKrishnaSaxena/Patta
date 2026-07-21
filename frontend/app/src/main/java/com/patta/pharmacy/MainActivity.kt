package com.patta.pharmacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.patta.pharmacy.ui.navigation.PattaNavGraph
import com.patta.pharmacy.ui.theme.PattaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PattaTheme {
                PattaNavGraph()
            }
        }
    }
}
