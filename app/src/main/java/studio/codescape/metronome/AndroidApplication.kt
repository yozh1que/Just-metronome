package studio.codescape.metronome

import android.content.Context
import kotlinx.coroutines.Dispatchers
import studio.codescape.metronome.common.di.CoroutineDispatchers
import studio.codescape.metronome.di.AppComponent
import studio.codescape.metronome.di.SessionComponent
import studio.codescape.metronome.di.create
import studio.codescape.metronome.domain.model.JustMetronomeApplication

class AndroidApplication : android.app.Application() {

    internal lateinit var justMetronomeApplication: JustMetronomeApplication

    override fun onCreate() {
        super.onCreate()
        justMetronomeApplication = getJustMetronomeApplication()
    }

    private fun getJustMetronomeApplication(): JustMetronomeApplication =
        JustMetronomeApplication(
            coroutineDispatchers = CoroutineDispatchers(
                main = Dispatchers.Main,
                io = Dispatchers.IO,
                computation = Dispatchers.Default
            ),
            createAppComponent = { coroutineDispatchers ->
                AppComponent::class.create(
                    applicationContext,
                    coroutineDispatchers
                )
            },
            createSessionComponent = { appComponent, parentCoroutineContext ->
                SessionComponent::class.create(
                    appComponent = appComponent,
                    parentCoroutineContext = parentCoroutineContext,
                )
            }
        )
}

val Context.justMetronomeApp
    get() = (this.applicationContext as AndroidApplication).justMetronomeApplication