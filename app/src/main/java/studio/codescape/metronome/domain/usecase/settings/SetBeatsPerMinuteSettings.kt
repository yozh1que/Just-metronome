package studio.codescape.metronome.domain.usecase.settings

interface SetBeatsPerMinuteSettings {
    suspend operator fun invoke(beatsPerMinute: Int)
}