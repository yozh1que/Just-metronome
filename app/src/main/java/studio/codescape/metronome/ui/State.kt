package studio.codescape.metronome.ui

import androidx.annotation.DrawableRes
import studio.codescape.metronome.domain.model.Metronome

data class State(
    @DrawableRes
    val mainIconRes: Int,
    val beatsPerMinuteLabel: String
)