package dev.nucleus.scheduleit.data.drive

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.driveDataStore by preferencesDataStore(name = "scheduleit_drive")

internal data class StoredAndroidTokens(
    val accountName: String,
    val email: String?,
    val accessToken: String,
    val expiresAtEpochSec: Long,
    val backupFileId: String?,
    val lastBackupEpochSec: Long?,
)

internal class AndroidTokenStore(private val context: Context) {

    suspend fun load(): StoredAndroidTokens? {
        val prefs = context.driveDataStore.data.first()
        val account = prefs[KEY_ACCOUNT] ?: return null
        val token = prefs[KEY_ACCESS_TOKEN] ?: return null
        return StoredAndroidTokens(
            accountName = account,
            email = prefs[KEY_EMAIL],
            accessToken = token,
            expiresAtEpochSec = prefs[KEY_EXPIRES_AT] ?: 0L,
            backupFileId = prefs[KEY_BACKUP_FILE_ID],
            lastBackupEpochSec = prefs[KEY_LAST_BACKUP],
        )
    }

    suspend fun save(tokens: StoredAndroidTokens) {
        context.driveDataStore.edit { prefs ->
            prefs[KEY_ACCOUNT] = tokens.accountName
            prefs[KEY_ACCESS_TOKEN] = tokens.accessToken
            prefs[KEY_EXPIRES_AT] = tokens.expiresAtEpochSec
            tokens.email?.let { prefs[KEY_EMAIL] = it } ?: prefs.remove(KEY_EMAIL)
            tokens.backupFileId?.let { prefs[KEY_BACKUP_FILE_ID] = it } ?: prefs.remove(KEY_BACKUP_FILE_ID)
            tokens.lastBackupEpochSec?.let { prefs[KEY_LAST_BACKUP] = it } ?: prefs.remove(KEY_LAST_BACKUP)
        }
    }

    suspend fun clear() {
        context.driveDataStore.edit { it.clear() }
    }

    companion object Keys {
        private val KEY_ACCOUNT: Preferences.Key<String> = stringPreferencesKey("account_name")
        private val KEY_EMAIL: Preferences.Key<String> = stringPreferencesKey("email")
        private val KEY_ACCESS_TOKEN: Preferences.Key<String> = stringPreferencesKey("access_token")
        private val KEY_EXPIRES_AT: Preferences.Key<Long> = longPreferencesKey("expires_at")
        private val KEY_BACKUP_FILE_ID: Preferences.Key<String> = stringPreferencesKey("backup_file_id")
        private val KEY_LAST_BACKUP: Preferences.Key<Long> = longPreferencesKey("last_backup")
    }
}
