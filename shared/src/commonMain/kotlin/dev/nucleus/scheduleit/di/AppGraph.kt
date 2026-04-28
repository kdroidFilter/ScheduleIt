package dev.nucleus.scheduleit.di

import androidx.lifecycle.ViewModel
import dev.nucleus.scheduleit.data.DriverFactory
import dev.nucleus.scheduleit.data.ScheduleRepository
import dev.nucleus.scheduleit.data.drive.GoogleDriveSync
import dev.nucleus.scheduleit.data.drive.createGoogleDriveSync
import dev.nucleus.scheduleit.db.ScheduleDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlin.reflect.KClass

@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {

    val viewModelFactory: MetroViewModelFactory
    val repository: ScheduleRepository

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(driverFactory: DriverFactory): ScheduleDatabase =
        ScheduleDatabase(driverFactory.createDriver())

    @Provides
    @SingleIn(AppScope::class)
    fun provideGoogleDriveSync(): GoogleDriveSync? = createGoogleDriveSync()

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides driverFactory: DriverFactory): AppGraph
    }
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AppMetroViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel>,
    override val assistedFactoryProviders: Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory>,
    override val manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory>,
) : MetroViewModelFactory()
