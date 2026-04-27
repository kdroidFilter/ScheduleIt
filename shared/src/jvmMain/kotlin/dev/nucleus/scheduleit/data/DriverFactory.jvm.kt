package dev.nucleus.scheduleit.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.nucleus.scheduleit.db.ScheduleDatabase
import java.io.File
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val home = System.getProperty("user.home").orEmpty()
        val dir = File(home, ".scheduleit").apply { mkdirs() }
        val path = File(dir, "scheduleit.db").absolutePath
        val properties = Properties().apply {
            setProperty("journal_mode", "WAL")
            setProperty("busy_timeout", "5000")
            setProperty("synchronous", "NORMAL")
        }
        return JdbcSqliteDriver(
            "jdbc:sqlite:$path",
            properties,
            ScheduleDatabase.Schema,
        )
    }
}
