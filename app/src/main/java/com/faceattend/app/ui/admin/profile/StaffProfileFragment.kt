package com.faceattend.app.ui.admin.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.faceattend.app.R
import com.faceattend.app.databinding.FragmentStaffProfileBinding
import com.faceattend.app.ui.appViewModels
import com.faceattend.app.ui.common.StaffDetailViewModel
import java.io.File

class StaffProfileFragment : Fragment() {

    private var _binding: FragmentStaffProfileBinding? = null
    private val binding get() = _binding!!

    private val args: StaffProfileFragmentArgs by navArgs()

    private val viewModel by appViewModels { container ->
        StaffDetailViewModel(container.staffRepository, args.staffId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.enrolFaceButton.setOnClickListener {
            findNavController().navigate(
                StaffProfileFragmentDirections.actionStaffProfileToEnrollFace(args.staffId)
            )
        }
        binding.historyButton.setOnClickListener {
            findNavController().navigate(
                StaffProfileFragmentDirections.actionStaffProfileToAttendanceHistory(args.staffId)
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
            binding.enrolFaceButton.setText(
                if (staff.hasEnrolledFace) R.string.action_reenrol_face else R.string.action_enrol_face
            )
            staff.faceImagePath?.let { binding.faceImage.load(File(it)) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
