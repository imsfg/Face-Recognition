package com.faceattend.app.ui.admin.stafflist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.faceattend.app.R
import com.faceattend.app.databinding.FragmentStaffListBinding
import com.faceattend.app.ui.appViewModels

class StaffListFragment : Fragment() {

    private var _binding: FragmentStaffListBinding? = null
    private val binding get() = _binding!!

    private val viewModel by appViewModels { container ->
        StaffListViewModel(container.staffRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = StaffListAdapter { staff ->
            findNavController().navigate(
                StaffListFragmentDirections.actionStaffListToStaffProfile(staff.id)
            )
        }
        binding.staffRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.staffRecycler.adapter = adapter

        binding.addStaffButton.setOnClickListener {
            findNavController().navigate(R.id.action_staffList_to_addStaff)
        }

        viewModel.staff.observe(viewLifecycleOwner) { staff ->
            adapter.submitList(staff)
            binding.emptyText.visibility = if (staff.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.staffRecycler.adapter = null
        _binding = null
    }
}
