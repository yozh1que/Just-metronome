package studio.codescape.metronome.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import studio.codescape.metronome.conductor.domain.model.Conductor
import studio.codescape.metronome.conductor.domain.usecase.CreateConductor
import studio.codescape.metronome.player.domain.model.Player
import studio.codescape.metronome.player.domain.usecase.CreatePlayer
import kotlin.coroutines.CoroutineContext

class Metronome(
    private val createConductor: CreateConductor,
    private val createPlayer: CreatePlayer,
    parentCoroutineContext: CoroutineContext
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = parentCoroutineContext + Job()



}