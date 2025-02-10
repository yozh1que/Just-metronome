package studio.codescape.metronome.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import studio.codescape.metronome.domain.model.Application
import studio.codescape.metronome.getMetronomeApplication

typealias JustMetronome = @Composable () -> Unit


sealed interface Screen {

    @Serializable
    data object Metronome : Screen
}

@Composable
fun JustMetronome(
    application: Application = rememberApplication()
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
fun rememberApplication(): Application {
    val context = LocalContext.current
    return remember {
        context.applicationContext.getMetronomeApplication()
    }
}




