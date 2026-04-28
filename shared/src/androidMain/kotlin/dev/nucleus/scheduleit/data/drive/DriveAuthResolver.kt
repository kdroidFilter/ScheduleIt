package dev.nucleus.scheduleit.data.drive

import android.content.Intent
import android.content.IntentSender

/**
 * Bridge between the data-layer Drive sync (which has no Activity) and the
 * UI layer (which can launch ActivityResult contracts).
 *
 * MainActivity registers an [ActivityResultLauncher] for `StartIntentSenderForResult`
 * and provides an implementation of this interface. The sync class then suspends
 * on [resolve] when an authorization PendingIntent has to be presented.
 */
interface DriveAuthResolver {
    suspend fun resolve(intentSender: IntentSender): Intent?
}

/**
 * Singleton holder so the Metro graph can build the sync without already having
 * the activity. The Activity sets this at onCreate; the sync reads it on demand.
 */
object DriveAuthResolverHolder {
    @Volatile
    var resolver: DriveAuthResolver? = null
}
