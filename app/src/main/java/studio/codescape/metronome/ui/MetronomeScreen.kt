package studio.codescape.metronome.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.common.ui.viewModel
import studio.codescape.metronome.ui.theme.JustMetronomeTheme

@Inject
@Composable
fun MetronomeScreen(getMetronomeViewModel: () -> MetronomeViewModel) {
    val viewModel = viewModel(getMetronomeViewModel)
    val state by viewModel.state.collectAsStateWithLifecycle()
    MetronomeScreen(
        onTogglePlaybackClick = { viewModel.handleCommand(MetronomeViewModel.Command.TogglePlayback) },
        onBeatsPerMinuteChange = { value ->
            viewModel.handleCommand(
                MetronomeViewModel.Command.SetBeatsPerMinute(
                    value
                )
            )
        },
        beatsPerMinute = state.beatsPerMinuteLabel,
    )
}

@Composable
private fun MetronomeScreen(
    modifier: Modifier = Modifier,
    onTogglePlaybackClick: () -> Unit,
    onBeatsPerMinuteChange: (Int) -> Unit,
    beatsPerMinute: String,
) = Scaffold(modifier = modifier) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Text(beatsPerMinute)
        Button(onClick = onTogglePlaybackClick) {
            Text("Play/Pause")
        }
    }
}

@Preview
@Composable
private fun PreviewMetronomeScreen() = JustMetronomeTheme {
    MetronomeScreen(
        onTogglePlaybackClick = {},
        onBeatsPerMinuteChange = {},
        beatsPerMinute = "120"
    )
}

typealias MetronomeScreen = @Composable () -> Unit