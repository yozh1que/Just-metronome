package studio.codescape.metronome.ui

import androidx.annotation.DrawableRes

data class State(
    @DrawableRes
    val mainIconRes: Int,
    val beatsPerMinuteLabel: String
)