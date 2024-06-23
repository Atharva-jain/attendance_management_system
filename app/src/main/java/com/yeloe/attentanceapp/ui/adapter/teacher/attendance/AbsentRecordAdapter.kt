package com.yeloe.attentanceapp.ui.adapter.teacher.attendance

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

class AbsentRecordAdapter(
    private val listener: OnMarkedPresentListener
) : RecyclerView.Adapter<AbsentRecordAdapter.AbsentRecordHolder>() {

    inner class AbsentRecordHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val markedPresentDateTextView: TextView =
            itemView.findViewById(R.id.markedPresentDateTextView)
        val markedPresentTimeTextView: TextView =
            itemView.findViewById(R.id.markedPresentTimeTextView)
        val markedPresentAddressTextView: TextView =
            itemView.findViewById(R.id.markedPresentAddressTextView)
        val markedPresentButtonCardView: MaterialCardView =
            itemView.findViewById(R.id.markedPresentButtonCardView)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsentRecordHolder {
        return AbsentRecordHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.marked_adsent_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: AbsentRecordHolder, position: Int) {
        val model = mDiffer.currentList[position]
        val date = GetDateAndTime.getCurrentDate(model.timeStamp)
        val time = GetDateAndTime.getCurrentTime(model.timeStamp)

        holder.markedPresentDateTextView.text = date
        holder.markedPresentTimeTextView.text = time
        holder.markedPresentAddressTextView.text = "Location: ${model.location.address}"

        holder.markedPresentButtonCardView.setOnClickListener {
            listener.onMarkPresent(model)
        }
    }
}

interface OnMarkedPresentListener {
    fun onMarkPresent(markedAttendance: MarkedAttendance)
}