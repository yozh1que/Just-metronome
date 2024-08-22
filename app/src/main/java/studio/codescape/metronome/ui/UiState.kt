package studio.codescape.metronome.ui

import androidx.annotation.DrawableRes

data class UiState(
    val mainIcon: MainIcon,
    val beatsPerMinuteLabel: String
) {
    sealed interface MainIcon {
        data object IndeterminateProgress : MainIcon

        @JvmInline
        value class Drawable(
            @DrawableRes
            val iconRes: Int
        ) : MainIcon
    }
}