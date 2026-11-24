package es.technical_task_iberia_android

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.WindowCompat
import es.technical_task_iberia_android.navigation.StarshipsNavHost
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            StarshipsTheme {
                val nightMode = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK
                val isDarkTheme = nightMode == Configuration.UI_MODE_NIGHT_YES
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                StarshipsNavHost(onDetailVisibilityChanged = { isDetail ->
                    insetsController.isAppearanceLightNavigationBars = !isDetail && !isDarkTheme
                    window.isNavigationBarContrastEnforced = !isDetail
                })
            }
        }
    }
}
