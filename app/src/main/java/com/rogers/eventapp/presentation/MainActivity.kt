package com.rogers.eventapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil3.ImageLoader
import com.rogers.eventapp.presentation.navigation.EventsNavHost
import com.rogers.eventapp.presentation.ui.theme.AssignmentTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CompositionLocalProvider(
                LocalImageLoader provides imageLoader
            ) {
                AssignmentTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        EventsNavHost(
                            startDestination = intent?.data?.let { uri ->
                                val eventId = uri.lastPathSegment
                                if (eventId != null) "event_detail/$eventId" else null
                            }
                        )
                    }
                }
            }
        }
    }
}

val LocalImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("No ImageLoader provided")
}