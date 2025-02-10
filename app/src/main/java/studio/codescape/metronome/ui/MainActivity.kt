package studio.codescape.metronome.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import studio.codescape.metronome.ui.theme.JustMetronomeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JustMetronomeTheme {
                JustMetronome()
            }
        }
    }
}