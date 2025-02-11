package studio.codescape.metronome.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import studio.codescape.metronome.domain.model.JustMetronomeApplication
import studio.codescape.metronome.justMetronomeApp

typealias JustMetronome = @Composable () -> Unit


sealed interface Screen {

    @Serializable
    data object Metronome : Screen
}

@Composable
fun JustMetronome(
    justMetronomeApplication: JustMetronomeApplication = rememberApplication()
) {
    val navigationController: NavHostController = rememberNavController()

    NavHost(
        navController = navigationController,
        startDestination = Screen.Metronome
    ) {
        composable<Screen.Metronome> {
            MetronomeScreen(

            )
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




