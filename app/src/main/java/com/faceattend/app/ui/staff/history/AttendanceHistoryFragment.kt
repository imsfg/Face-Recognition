package com.faceattend.app.ui.staff.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.faceattend.app.data.repository.AttendanceRepository
import com.faceattend.app.databinding.FragmentAttendanceHistoryBinding
import com.faceattend.app.ui.appViewModels

class AttendanceHistoryViewModel(
    attendanceRepository: AttendanceRepository,
    staffId: Long
) : ViewModel() {
    val records = attendanceRepository.observeForStaff(staffId).asLiveData()
}

class AttendanceHistoryFragment : Fragment() {

    private var _binding: FragmentAttendanceHistoryBinding? = null
    private val binding get() = _binding!!

    private val args: AttendanceHistoryFragmentArgs by navArgs()

    private val viewModel by appViewModels { container ->
        AttendanceHistoryViewModel(container.attendanceRepository, args.staffId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = AttendanceHistoryAdapter()
        binding.historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecycler.adapter = adapter

        viewModel.records.observe(viewLifecycleOwner) { records ->
            adapter.submitList(records)
            binding.emptyText.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.historyRecycler.adapter = null
        _binding = null
    }
}
