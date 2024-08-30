package studio.codescape.metronome.player.domain.usecase.settings

interface SetSoundUriSettings {
    suspend operator fun invoke(uri: String)
}