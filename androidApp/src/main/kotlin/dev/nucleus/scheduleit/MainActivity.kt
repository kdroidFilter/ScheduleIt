package dev.nucleus.scheduleit

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dev.nucleus.scheduleit.data.drive.DriveAuthResolver
import dev.nucleus.scheduleit.data.drive.DriveAuthResolverHolder
import dev.nucleus.scheduleit.di.createAndroidAppGraph
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import dev.nucleus.scheduleit.updater.AndroidAppUpdater
import dev.nucleus.scheduleit.updater.AppUpdaterOverlay
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), DriveAuthResolver {

    private var pendingResolution: CompletableDeferred<Intent?>? = null

    private val authLauncher = registerForActivityResult(StartIntentSenderForResult()) { result ->
        // Pass the result Intent back to whoever called resolve(); a null here
        // means the user cancelled.
        pendingResolution?.complete(result.data)
        pendingResolution = null
    }

    override suspend fun resolve(intentSender: IntentSender): Intent? {
        val deferred = CompletableDeferred<Intent?>()
        pendingResolution = deferred
        authLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        return deferred.await()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Keep the system splash screen visible until the database has emitted
        // its first snapshot. Avoids the "no day selected" flash on cold start.
        val splashScreen = installSplashScreen()
        val ready = AtomicBoolean(false)
        splashScreen.setKeepOnScreenCondition { !ready.get() }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        DriveAuthResolverHolder.resolver = this

        // createAndroidAppGraph initialises FileKit with the activity's
        // result registry — must run before the ViewModel can launch
        // ExportData / ImportData intents.
        val graph = createAndroidAppGraph(this)

        lifecycleScope.launch {
            graph.repository.ensureDefaults()
            graph.repository.observeSchedule().first()
            ready.set(true)
        }

        val updater = AndroidAppUpdater(applicationContext)
        updater.start(lifecycleScope)

        setContent {
            Box(Modifier.fillMaxSize()) {
                App(graph)
                MobileTheme {
                    AppUpdaterOverlay(
                        state = updater.state.collectAsState(),
                        onInstall = { updater.install(lifecycleScope) },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        if (DriveAuthResolverHolder.resolver === this) {
            DriveAuthResolverHolder.resolver = null
        }
        super.onDestroy()
    }
}
