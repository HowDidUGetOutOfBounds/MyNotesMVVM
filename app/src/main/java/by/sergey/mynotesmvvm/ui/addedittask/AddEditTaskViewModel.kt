package by.sergey.mynotesmvvm.ui.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.sergey.mynotesmvvm.data.Task
import by.sergey.mynotesmvvm.data.TaskDao
import by.sergey.mynotesmvvm.ui.ADD_TASK_RESULT_OK
import by.sergey.mynotesmvvm.ui.EDIT_TASK_RESULT_OK
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val state: SavedStateHandle
) : ViewModel() {

    val task = state.get<Task>("task")
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }

    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("name can not be empty")
            return
        }

        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }


    private fun createTask(newTask: Task) {
        viewModelScope.launch {
            taskDao.insert(newTask)
            addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
        }
    }

    private fun updateTask(updatedTask: Task) {
        viewModelScope.launch {
            taskDao.update(updatedTask)
            addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
        }
    }

    private fun showInvalidInputMessage(text: String) {
        viewModelScope.launch {
            addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
        }
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}