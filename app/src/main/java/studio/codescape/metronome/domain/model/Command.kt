package studio.codescape.metronome.domain.model

interface Command {
    object Toggle : Command

    @JvmInline
    value class SetBeatsPerMinute(
        val beatsPerMinute: Int
    ) : Command
}