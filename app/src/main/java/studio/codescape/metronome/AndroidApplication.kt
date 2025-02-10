package studio.codescape.metronome

import android.content.Context
import studio.codescape.metronome.di.AppComponent
import studio.codescape.metronome.di.SessionComponent
import studio.codescape.metronome.di.create
import studio.codescape.metronome.domain.model.Application

class AndroidApplication : android.app.Application() {

    lateinit var application: Application

    override fun onCreate() {
        super.onCreate()
        application = getMetronomeApplication()
    }

    private fun getMetronomeApplication(): Application =
        Application(
            createAppComponent = { AppComponent::class.create(applicationContext) },
            createSessionComponent = { appComponent, parentCoroutineContext ->
                SessionComponent::class.create(
                    appComponent = appComponent,
                    parentCoroutineContext = parentCoroutineContext,
                )
            }
        )
}
fun Context.getMetronomeApplication(): Application = (this as AndroidApplication).application