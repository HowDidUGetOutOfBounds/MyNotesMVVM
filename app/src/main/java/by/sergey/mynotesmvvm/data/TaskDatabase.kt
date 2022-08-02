package by.sergey.mynotesmvvm.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import by.sergey.mynotesmvvm.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val appScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().taskDao()

            appScope.launch {
                dao.insert(Task("Buy"))
                dao.insert(Task("Call"))
                dao.insert(Task("Any"))
                dao.insert(Task("TestTask4", important = true))
                dao.insert(Task("TestTask5", completed = true))
                dao.insert(Task("TestTask6"))
                dao.insert(Task("TestTask7", completed = true))
                dao.insert(Task("TestTask8"))
            }
            //db operations
        }
    }
}