package studio.codescape.metronome.player.domain.usecase

interface SetSoundUri {
    operator fun invoke(uri: String)
}