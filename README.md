# ScheduleIt

A Kotlin Multiplatform app for designing your **recurring weekly schedule** — one template per weekday, color-coded events, notes, and notifications. Targets **Android, iOS, and Desktop (JVM)** with a shared Compose Multiplatform UI.

![Weekly overview](art/weekly-overview-context-menu.png)

## Features

- **Weekly grid view** — Monday through Sunday at a glance, with a configurable visible hours window.
- **Day templates** — every weekday has its own schedule, or share one across days with "Same as".
- **Color-coded events** with title, time range, and free-form notes.
- **Hover tooltips** showing event details inline.
- **Native notifications** to remind you of upcoming events.
- **JSON import/export** for backup and migration between devices.
- **Single-instance** desktop app with native window decorations (Jewel + Nucleus).

## Screenshots

**Hover tooltips** show an event’s time and notes inline:

![Hover tooltip](art/weekly-overview-tooltip.png)

**Edit dialog** for changing title, time, color, and notes:

![Edit event](art/edit-event-dialog.png)

**Settings** to configure visible hours and per-day templates:

![Settings](art/settings.png)

## Project structure

- [`/shared`](./shared/src) — Kotlin Multiplatform module shared across platforms.
    - [`commonMain`](./shared/src/commonMain/kotlin) — domain, data (SQLDelight), DI (Metro), presentation, Compose UI.
    - [`androidMain`](./shared/src/androidMain/kotlin), [`iosMain`](./shared/src/iosMain/kotlin), [`jvmMain`](./shared/src/jvmMain/kotlin) — platform-specific drivers and integrations.
- [`/androidApp`](./androidApp) — Android entry point (`MainActivity`, manifest, resources).
- [`/desktopApp`](./desktopApp) — Desktop (JVM) entry point (`main()`, distribution config).
- [`/iosApp`](./iosApp/iosApp) — iOS entry point (SwiftUI host for the Compose UI).

## Tech stack

- **Compose Multiplatform** + **Jewel** (IntelliJ-style theming on desktop)
- **SQLDelight** for type-safe SQL across platforms
- **Metro** for dependency injection
- **kotlinx.coroutines / Flow** for reactive state
- **kotlinx.serialization** for JSON backup/restore

Desktop data lives at `~/.scheduleit/scheduleit.db`.

## Build and Run

### Android

- on macOS/Linux
  ```shell
  ./gradlew :androidApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :androidApp:assembleDebug
  ```

### Desktop (JVM)

- on macOS/Linux
  ```shell
  ./gradlew :desktopApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :desktopApp:run
  ```

### iOS

Use the run configuration from the run widget in your IDE’s toolbar, or open the [`/iosApp`](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html).

## License

This project is licensed under the GNU General Public License v3.0 — see the [LICENSE](./LICENSE) file for details.
