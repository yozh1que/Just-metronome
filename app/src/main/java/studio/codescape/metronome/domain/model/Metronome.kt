package studio.codescape.metronome.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.conductor.domain.model.Conductor
import kotlin.coroutines.CoroutineContext

@Inject
class Metronome(
    private val conductor: Conductor,
//    private val createConductor: CreateConductor,
//    private val createPlayer: CreatePlayer,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = Job()



}