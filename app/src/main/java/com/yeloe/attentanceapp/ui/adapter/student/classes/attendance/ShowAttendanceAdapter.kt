package com.yeloe.attentanceapp.ui.adapter.student.classes.attendance

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
import com.yeloe.attentanceapp.ui.adapter.teacher.attendance.PresentRecordAdapter
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetDateAndTime

class ShowAttendanceAdapter(
    private val listener: IsEmptyAttendanceListener,
    options: FirestoreRecyclerOptions<MarkedAttendance>,
) : FirestoreRecyclerAdapter<MarkedAttendance, ShowAttendanceAdapter.ShowAttendanceHolder>(options) {

    inner class ShowAttendanceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationShowAttendanceTextView: TextView =
            itemView.findViewById(R.id.locationShowAttendanceTextView)
        val dateShowAttendanceTextView: TextView =
            itemView.findViewById(R.id.dateShowAttendanceTextView)
        val timeShowAttendanceTextView: TextView =
            itemView.findViewById(R.id.timeShowAttendanceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowAttendanceHolder {
        return ShowAttendanceHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.show_attendance_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ShowAttendanceHolder,
        position: Int,
        model: MarkedAttendance
    ) {
        val date = GetDateAndTime.getCurrentDate(model.timeStamp)
        val time = GetDateAndTime.getCurrentTime(model.timeStamp)

        holder.dateShowAttendanceTextView.text = date
        holder.timeShowAttendanceTextView.text = time
        holder.locationShowAttendanceTextView.text = "Location: ${model.location.address}"
    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (snapshots.isEmpty()) {
            listener.isEmptyAttendanceListener(true)
        } else {
            listener.isEmptyAttendanceListener(false)
        }
    }

    override fun onError(e: FirebaseFirestoreException) {
        super.onError(e)
        Log.d(Constant.TEACHER_LOG, "Error on PresentRecordAdapter $e")
    }

}

interface IsEmptyAttendanceListener {
    fun isEmptyAttendanceListener(value: Boolean)
}