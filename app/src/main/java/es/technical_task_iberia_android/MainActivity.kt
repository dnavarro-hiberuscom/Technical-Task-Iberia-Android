package es.technical_task_iberia_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import es.technical_task_iberia_android.navigation.StarshipsNavHost
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StarshipsTheme {
                StarshipsNavHost()
            }
        }
    }
}
