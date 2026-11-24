package es.technical_task_iberia_android

import android.app.Application
import es.technical_task_iberia_android.data.di.dataModule
import es.technical_task_iberia_android.presentation.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/** Composition root: the only place that brings `:data` and `:presentation` together. */
class StarshipsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@StarshipsApplication)
            modules(appModules)
        }
    }
}

/** Every Koin module of the app, in one list so the graph can be verified as a whole. */
val appModules = listOf(dataModule, presentationModule)
