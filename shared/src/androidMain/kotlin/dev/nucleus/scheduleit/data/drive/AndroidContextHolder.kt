package dev.nucleus.scheduleit.data.drive

import android.content.Context

/**
 * Holds the application context for use by classes constructed inside the
 * Metro graph (where wiring a Context through every provider is awkward).
 *
 * Set once in [dev.nucleus.scheduleit.di.createAndroidAppGraph].
 */
internal object AndroidContextHolder {
    @Volatile
    var applicationContext: Context? = null
}
