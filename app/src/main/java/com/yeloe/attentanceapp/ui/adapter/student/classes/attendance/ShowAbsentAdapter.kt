package com.yeloe.attentanceapp.ui.adapter.student.classes.attendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.model.student.MarkedAttendance
import com.yeloe.attentanceapp.utils.GetDateAndTime

class ShowAbsentAdapter : RecyclerView.Adapter<ShowAbsentAdapter.ShowAbsentHolder>() {

    inner class ShowAbsentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationShowAbsentTextView: TextView =
            itemView.findViewById(R.id.locationShowAbsentTextView)
        val dateShowAbsentTextView: TextView =
            itemView.findViewById(R.id.dateShowAbsentTextView)
        val timeShowAbsentTextView: TextView =
            itemView.findViewById(R.id.timeShowAbsentTextView)

    }

    private val mDiffer: AsyncListDiffer<MarkedAttendance>

    private val diffCallback = object : DiffUtil.ItemCallback<MarkedAttendance>() {
        override fun areItemsTheSame(
            oldItem: MarkedAttendance,
            newItem: MarkedAttendance
        ): Boolean {
            return oldItem.classUid == newItem.classUid
        }

        override fun areContentsTheSame(
            oldItem: MarkedAttendance,
            newItem: MarkedAttendance
        ): Boolean {
            return oldItem == newItem
        }

    }

    init {
        mDiffer = AsyncListDiffer(this, diffCallback)
    }

    fun submitList(data: List<MarkedAttendance>) {
        mDiffer.submitList(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowAbsentHolder {
        return ShowAbsentHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.show_absent_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: ShowAbsentHolder, position: Int) {
        val model = mDiffer.currentList[position]
        val date = GetDateAndTime.getCurrentDate(model.timeStamp)
        val time = GetDateAndTime.getCurrentTime(model.timeStamp)

        holder.dateShowAbsentTextView.text = date
        holder.timeShowAbsentTextView.text = time
        holder.locationShowAbsentTextView.text = "Location: ${model.location.address}"

    }

}