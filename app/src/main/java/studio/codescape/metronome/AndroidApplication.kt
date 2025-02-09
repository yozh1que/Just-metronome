package studio.codescape.metronome

import android.app.Application
import studio.codescape.metronome.di.AppComponent
import studio.codescape.metronome.di.SessionComponent
import studio.codescape.metronome.di.create

class AndroidApplication : Application() {

    private lateinit var metronomeApplication: MetronomeApplication

    override fun onCreate() {
        super.onCreate()
        metronomeApplication = getMetronomeApplication()
    }

    private fun getMetronomeApplication(): MetronomeApplication =
        AppComponent::class.create(applicationContext).let { appComponent ->
            MetronomeApplication(
                createAppComponent = { appComponent },
                createSessionComponent = { parentCoroutineContext ->
                    SessionComponent::class.create(
                        appComponent = appComponent,
                        parentCoroutineContext = parentCoroutineContext,
                    )
                }
            )
        }

}