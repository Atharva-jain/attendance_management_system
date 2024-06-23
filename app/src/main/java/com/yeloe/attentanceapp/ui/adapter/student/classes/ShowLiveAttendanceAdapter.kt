package com.yeloe.attentanceapp.ui.adapter.student.classes

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
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.LiveClassesAdapter
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetDateAndTime

class ShowLiveAttendanceAdapter(
    private val lister: LiveAttendanceListener,
    options: FirestoreRecyclerOptions<Class>,
) : FirestoreRecyclerAdapter<Class, ShowLiveAttendanceAdapter.ShowLiveAttendanceHolder>(options) {

    inner class ShowLiveAttendanceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val markAttendanceButton: MaterialCardView =
            itemView.findViewById(R.id.studentMarkAttendanceButtonCardView)
        val studentLiveClassDateTextView: TextView =
            itemView.findViewById(R.id.studentLiveClassDateTextView)
        val studentLiveClassTimeTextView: TextView =
            itemView.findViewById(R.id.studentLiveClassTimeTextView)
        val studentLocationLiveClassTextView: TextView =
            itemView.findViewById(R.id.studentLocationLiveClassTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowLiveAttendanceHolder {
        return ShowLiveAttendanceHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.live_class_mark_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ShowLiveAttendanceHolder, position: Int, model: Class) {
        holder.markAttendanceButton.setOnClickListener {
            lister.markAttendance(classes = model)
        }
        val date = GetDateAndTime.getCurrentDate(model.timeStamp)
        val time = GetDateAndTime.getCurrentDate(model.timeStamp)

        holder.studentLiveClassDateTextView.text = date
        holder.studentLiveClassTimeTextView.text = time
        holder.studentLocationLiveClassTextView.text = "Location: ${model.location.address}"
    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (snapshots.isEmpty()) {
            lister.isLiveDataEmpty(true)
        } else {
            lister.isLiveDataEmpty(false)
        }
    }

    override fun onError(e: FirebaseFirestoreException) {
        super.onError(e)
        Log.d(Constant.TEACHER_LOG,"Error on ShowLiveAttendanceAdapter $e")
    }

}

interface LiveAttendanceListener {
    fun markAttendance(classes: Class)
    fun isLiveDataEmpty(value: Boolean)
}