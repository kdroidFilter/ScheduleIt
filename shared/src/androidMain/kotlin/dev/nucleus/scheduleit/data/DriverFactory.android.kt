package dev.nucleus.scheduleit.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.nucleus.scheduleit.db.ScheduleDatabase

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(ScheduleDatabase.Schema, context, "scheduleit.db")
}
