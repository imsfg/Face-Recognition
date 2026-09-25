package com.faceattend.app.ui.staff.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.faceattend.app.R
import com.faceattend.app.appContainer
import com.faceattend.app.databinding.FragmentStaffHomeBinding
import com.faceattend.app.ui.appViewModels
import com.faceattend.app.ui.common.StaffDetailViewModel

class StaffHomeFragment : Fragment() {

    private var _binding: FragmentStaffHomeBinding? = null
    private val binding get() = _binding!!

    private val staffId by lazy { requireContext().appContainer.sessionManager.staffId }

    private val viewModel by appViewModels { container ->
        StaffDetailViewModel(container.staffRepository, container.sessionManager.staffId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.markAttendanceButton.setOnClickListener {
            findNavController().navigate(R.id.action_staffHome_to_markAttendance)
        }
        binding.historyButton.setOnClickListener {
            findNavController().navigate(
                StaffHomeFragmentDirections.actionStaffHomeToAttendanceHistory(staffId)
            )
        }

        viewModel.staff.observe(viewLifecycleOwner) { staff ->
            staff ?: return@observe
            binding.nameText.text = staff.name
            binding.employeeIdText.text = staff.employeeId
            binding.enrollmentStatusText.setText(
                if (staff.hasEnrolledFace) R.string.label_enrolled else R.string.label_not_enrolled
            )
            binding.enrollmentStatusText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (staff.hasEnrolledFace) R.color.success else R.color.failure
                )
            )
            binding.markAttendanceButton.isEnabled = staff.hasEnrolledFace
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
