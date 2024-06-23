package com.yeloe.attentanceapp.ui.adapter.teacher.attendance

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestoreException
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.model.student.MarkedAttendance
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetDateAndTime

class PresentRecordAdapter(
    private val listener: OnMarkedAbsentListener,
    options: FirestoreRecyclerOptions<MarkedAttendance>,
) : FirestoreRecyclerAdapter<MarkedAttendance, PresentRecordAdapter.PresentRecordHolder>(options) {

    inner class PresentRecordHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val markedAbsentDateTextView: TextView =
            itemView.findViewById(R.id.markedAbsentDateTextView)
        val markedAbsentTimeTextView: TextView =
            itemView.findViewById(R.id.markedAbsentTimeTextView)
        val markedAbsentAddressTextView: TextView =
            itemView.findViewById(R.id.markedAbsentAddressTextView)
        val markedAbsentButtonCardView: MaterialCardView =
            itemView.findViewById(R.id.markedAbsentButtonCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresentRecordHolder {
        return PresentRecordHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.marked_present_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: PresentRecordHolder, position: Int, model: MarkedAttendance
    ) {

        val date = GetDateAndTime.getCurrentDate(model.timeStamp)
        val time = GetDateAndTime.getCurrentTime(model.timeStamp)

        holder.markedAbsentDateTextView.text = date
        holder.markedAbsentTimeTextView.text = time
        holder.markedAbsentAddressTextView.text = "Location: ${model.location.address}"

        holder.markedAbsentButtonCardView.setOnClickListener {
            listener.onMarkAbsent(model)
        }

    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (snapshots.isEmpty()) {
            listener.isAbsentDataEmpty(true)
        } else {
            listener.isAbsentDataEmpty(false)
        }
    }

    override fun onError(e: FirebaseFirestoreException) {
        super.onError(e)
        Log.d(Constant.TEACHER_LOG, "Error on PresentRecordAdapter $e")
    }

}

interface OnMarkedAbsentListener {
    fun onMarkAbsent(markedAttendance: MarkedAttendance)
    fun isAbsentDataEmpty(value: Boolean)
}