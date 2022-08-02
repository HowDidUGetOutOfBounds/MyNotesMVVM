package by.sergey.mynotesmvvm.di

import android.content.Context
import androidx.room.Room
import by.sergey.mynotesmvvm.data.TaskDao
import by.sergey.mynotesmvvm.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    fun provideTaskDao(taskDatabase: TaskDatabase): TaskDao {
        return taskDatabase.taskDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext appContext: Context,
        callback: TaskDatabase.Callback
    ): TaskDatabase {
        return Room.databaseBuilder(appContext, TaskDatabase::class.java, "TaskDatabase")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()
    }

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope