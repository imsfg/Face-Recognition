package com.faceattend.app.ui.staff.result

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
import com.faceattend.app.data.entities.RESULT_MATCHED
import com.faceattend.app.databinding.FragmentAttendanceResultBinding
import com.faceattend.app.ui.appViewModels
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class AttendanceResultFragment : Fragment() {

    private var _binding: FragmentAttendanceResultBinding? = null
    private val binding get() = _binding!!

    private val args: AttendanceResultFragmentArgs by navArgs()

    private val viewModel by appViewModels { container ->
        AttendanceResultViewModel(container.attendanceRepository, args.attendanceId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.doneButton.setOnClickListener { findNavController().popBackStack() }

        viewModel.record.observe(viewLifecycleOwner) { record ->
            record ?: return@observe
            val matched = record.result == RESULT_MATCHED

            binding.resultText.setText(
                if (matched) R.string.result_matched else R.string.result_not_matched
            )
            binding.resultText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (matched) R.color.success else R.color.failure
                )
            )
            binding.resultDetailText.visibility = if (matched) View.GONE else View.VISIBLE
            binding.resultDetailText.setText(R.string.result_not_matched_detail)

            binding.selfieImage.load(File(record.selfieImagePath))
            binding.similarityText.text =
                getString(R.string.label_similarity, record.matchSimilarity)
            binding.timeText.text = getString(
                R.string.label_time,
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(Date(record.timestamp))
            )
            binding.locationText.text = getString(
                R.string.label_location,
                formatLocation(record.latitude, record.longitude)
            )
        }
    }

    private fun formatLocation(latitude: Double?, longitude: Double?): String =
        if (latitude != null && longitude != null) {
            String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
        } else {
            getString(R.string.location_unavailable)
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
