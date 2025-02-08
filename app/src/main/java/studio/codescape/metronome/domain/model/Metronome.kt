package studio.codescape.metronome.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.conductor.domain.model.Conductor
import studio.codescape.metronome.di.SessionScope
import kotlin.coroutines.CoroutineContext

@SessionScope
@Inject
class Metronome(
    private val conductor: Conductor,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = Job()



}