package com.faceattend.app.ui.admin.stafflist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.faceattend.app.R
import com.faceattend.app.data.entities.Staff
import com.faceattend.app.databinding.ItemStaffBinding

class StaffListAdapter(
    private val onClick: (Staff) -> Unit
) : ListAdapter<Staff, StaffListAdapter.StaffViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StaffViewHolder(
        ItemStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class StaffViewHolder(
        private val binding: ItemStaffBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(staff: Staff) = with(binding) {
            nameText.text = staff.name
            employeeIdText.text = staff.employeeId
            enrollmentText.setText(
                if (staff.hasEnrolledFace) R.string.staff_row_enrolled else R.string.staff_row_pending
            )
            enrollmentText.setTextColor(
                ContextCompat.getColor(
                    root.context,
                    if (staff.hasEnrolledFace) R.color.success else R.color.failure
                )
            )
            root.setOnClickListener { onClick(staff) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Staff>() {
            override fun areItemsTheSame(oldItem: Staff, newItem: Staff) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Staff, newItem: Staff) = oldItem == newItem
        }
    }
}
