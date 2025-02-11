package studio.codescape.metronome.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import studio.codescape.metronome.domain.model.JustMetronomeApplication
import studio.codescape.metronome.justMetronomeApp


sealed interface Screen {

    @Serializable
    data object Metronome : Screen
}

@Composable
fun JustMetronomeScreen(
    justMetronomeApplication: JustMetronomeApplication = rememberApplication()
) {

    val appState by justMetronomeApplication.state.collectAsStateWithLifecycle()


    when(appState) {
        is JustMetronomeApplication.State.Initializing -> {

        }
        is JustMetronomeApplication.State.Ready.AppReady -> {
            LaunchedEffect(Unit) {
                justMetronomeApplication.handleCommand(JustMetronomeApplication.Command.StartSession)
            }
        }
        is JustMetronomeApplication.State.Ready.SessionReady -> {
            val navigationController: NavHostController = rememberNavController()
            NavHost(
                navController = navigationController,
                startDestination = Screen.Metronome
            ) {
                composable<Screen.Metronome> {
                    (appState as JustMetronomeApplication.State.Ready.SessionReady)
                        .sessionComponent.metronomeScreen()
                }
            }
        }
    }

}

@Composable
fun rememberApplication(): JustMetronomeApplication {
    val context = LocalContext.current
    return remember {
        context.applicationContext.justMetronomeApp
    }
}




