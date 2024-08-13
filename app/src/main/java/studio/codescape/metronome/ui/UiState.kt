package studio.codescape.metronome.ui

import androidx.annotation.DrawableRes

data class UiState(
    @DrawableRes
    val mainIconRes: Int,
    val beatsPerMinuteLabel: String
)