package by.sergey.mynotesmvvm.ui.tasks


import androidx.lifecycle.*
import by.sergey.mynotesmvvm.data.PreferencesManager
import by.sergey.mynotesmvvm.data.SortOrder
import by.sergey.mynotesmvvm.data.Task
import by.sergey.mynotesmvvm.data.TaskDao
import by.sergey.mynotesmvvm.ui.ADD_TASK_RESULT_OK
import by.sergey.mynotesmvvm.ui.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    )
    { query, preferences ->
        Triple(query, preferences.sortOrder, preferences.hideCompleted)
    }.flatMapLatest {
        taskDao.getTasks(it.first, it.second, it.third)
    }

    val tasks = taskFlow.asLiveData()

    fun onSortOrderSelected(sortOder: SortOrder) {
        viewModelScope.launch {
            preferencesManager.updateSortOrder(sortOder)
        }
    }

    fun onHideCompletedClick(state: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateHideCompleted(state)
        }
    }

    fun onTaskSelected(task: Task) {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
        }
    }

    fun onTaskCheckChanged(task: Task, isChecked: Boolean) {
        viewModelScope.launch {
            taskDao.update(task.copy(completed = isChecked))
        }
    }

    fun onTaskSwiped(task: Task) {
        viewModelScope.launch {
            taskDao.delete(task)
            tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
        }
    }

    fun onUndoDeleteClick(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }

    fun onAddNewTaskClick() {
        viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
        }
    }

    fun onAddEditResult(result: Int) {
        when(result)
        {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(string: String) {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(string))
        }
    }

    fun onDeleteAllCompletedtasks() {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)
        }
    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String): TasksEvent()
        object NavigateToDeleteAllCompletedScreen: TasksEvent()
    }
}
