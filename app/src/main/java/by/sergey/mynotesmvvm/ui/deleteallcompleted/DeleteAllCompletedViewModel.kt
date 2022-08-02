package by.sergey.mynotesmvvm.ui.deleteallcompleted

import androidx.lifecycle.ViewModel
import by.sergey.mynotesmvvm.data.TaskDao
import by.sergey.mynotesmvvm.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedViewModel @Inject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
    ) : ViewModel() {

        fun onConfirmClick() = applicationScope.launch {
            taskDao.deleteCompletedTasks()
        }
}