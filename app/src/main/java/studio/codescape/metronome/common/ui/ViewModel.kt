package studio.codescape.metronome.common.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.viewModelFactory

@Composable
inline fun <reified VM : ViewModel> viewModel(crossinline getViewModel: () -> VM): VM =
    androidx.lifecycle.viewmodel.compose.viewModel(
        factory = viewModelFactory {
            addInitializer(VM::class) {
                getViewModel()
            }
        }
    )