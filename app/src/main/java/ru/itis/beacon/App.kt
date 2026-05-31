package ru.itis.beacon

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.itis.core.domain.initializer.InitialDataLoader
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var initialDataLoader: InitialDataLoader

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)

        applicationScope.launch {
            initialDataLoader.initialize()
        }
    }
}