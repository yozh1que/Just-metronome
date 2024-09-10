package studio.codescape.metronome.conductor.domain.usecase.settings

interface SetBeatsPerMinuteSettings {
    suspend operator fun invoke(beatsPerMinute: Int)
}