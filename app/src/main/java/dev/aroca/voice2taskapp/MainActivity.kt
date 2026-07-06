package dev.aroca.voice2taskapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aroca.voice2taskapp.navigation.NavGraph
import dev.aroca.voice2taskapp.ui.theme.Voice2TaskTheme
import dev.aroca.voice2taskapp.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Voice2TaskTheme {
                val authViewModel: AuthViewModel = viewModel()
                NavGraph(authViewModel = authViewModel)
            }
        }
    }
}