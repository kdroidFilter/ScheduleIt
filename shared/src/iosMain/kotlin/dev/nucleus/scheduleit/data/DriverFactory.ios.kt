package dev.nucleus.scheduleit.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import dev.nucleus.scheduleit.db.ScheduleDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(ScheduleDatabase.Schema, "scheduleit.db")
}
