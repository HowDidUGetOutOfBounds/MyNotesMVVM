package by.sergey.mynotesmvvm.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.sergey.mynotesmvvm.R
import by.sergey.mynotesmvvm.databinding.FragmentAddEditBinding
import by.sergey.mynotesmvvm.databinding.FragmentTasksBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit) {

    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditBinding.bind(view)

        binding.apply {
            edit_text_task_name.setText(viewModel.taskName)
            checkbox_important.isChecked = viewModel.taskImportance
            checkbox_important.jumpDrawablesToCurrentState()
            textView_date_created.isVisible = viewModel.task != null
            textView_date_created.text = "Created: ${viewModel.task?.createdDateFormatted}"

            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            checkboxImportant.setOnCheckedChangeListener{ _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when(event)
                {
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

    }
}