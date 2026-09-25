package com.faceattend.app.ui.admin.addstaff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.faceattend.app.R
import com.faceattend.app.databinding.FragmentAddStaffBinding
import com.faceattend.app.ui.appViewModels
import com.google.android.material.snackbar.Snackbar

class AddStaffFragment : Fragment() {

    private var _binding: FragmentAddStaffBinding? = null
    private val binding get() = _binding!!

    private val viewModel by appViewModels { container ->
        AddStaffViewModel(container.staffRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.saveButton.setOnClickListener {
            viewModel.save(
                binding.nameInput.text?.toString().orEmpty(),
                binding.employeeIdInput.text?.toString().orEmpty()
            )
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                AddStaffState.Saved -> {
                    Toast.makeText(requireContext(), R.string.staff_added, Toast.LENGTH_SHORT).show()
                    viewModel.onStateHandled()
                    findNavController().popBackStack()
                }

                is AddStaffState.FieldError -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }

                is AddStaffState.Failure -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }

                AddStaffState.Idle -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
