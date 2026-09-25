package com.faceattend.app.ui.staff.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.faceattend.app.R
import com.faceattend.app.data.entities.AttendanceRecord
import com.faceattend.app.data.entities.RESULT_MATCHED
import com.faceattend.app.databinding.ItemAttendanceBinding
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class AttendanceHistoryAdapter : ListAdapter<AttendanceRecord, AttendanceHistoryAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class ViewHolder(
        private val binding: ItemAttendanceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: AttendanceRecord) = with(binding) {
            val context = root.context
            val matched = record.result == RESULT_MATCHED

            selfieThumb.load(File(record.selfieImagePath))
            timeText.text = DATE_FORMAT.format(Date(record.timestamp))
            locationText.text = if (record.latitude != null && record.longitude != null) {
                String.format(Locale.US, "%.5f, %.5f", record.latitude, record.longitude)
            } else {
                context.getString(R.string.location_unavailable)
            }
            resultText.setText(if (matched) R.string.result_matched else R.string.result_not_matched)
            resultText.setTextColor(
                ContextCompat.getColor(context, if (matched) R.color.success else R.color.failure)
            )
        }
    }

    private companion object {
        val DATE_FORMAT: DateFormat =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

        val DIFF = object : DiffUtil.ItemCallback<AttendanceRecord>() {
            override fun areItemsTheSame(oldItem: AttendanceRecord, newItem: AttendanceRecord) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: AttendanceRecord, newItem: AttendanceRecord) =
                oldItem == newItem
        }
    }
}
