package com.yeloe.attentanceapp.ui.adapter.teacher.classes

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
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetDateAndTime

class LiveClassesAdapter(
    private val listener: LiveClassListener,
    options: FirestoreRecyclerOptions<Class>,
) : FirestoreRecyclerAdapter<Class, LiveClassesAdapter.LiveClassHolder>(options) {

    inner class LiveClassHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val disableAttendanceButtonCardView: MaterialCardView =
            itemView.findViewById(R.id.disableAttendanceButtonCardView)
        val classNameLiveClassTextView: TextView =
            itemView.findViewById(R.id.classNameLiveClassTextView)
        val classCodeLiveClassTextView: TextView =
            itemView.findViewById(R.id.classCodeLiveClassTextView)
        val locationLiveClassTextView: TextView =
            itemView.findViewById(R.id.locationLiveClassTextView)
        val cancelClassButtonCardView: MaterialCardView =
            itemView.findViewById(R.id.cancelClassButtonCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveClassHolder {
        return LiveClassHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.teacher_live_class_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: LiveClassHolder, position: Int, model: Class) {
        holder.cancelClassButtonCardView.setOnClickListener {
            listener.removeClass(classes = model)
        }
        holder.disableAttendanceButtonCardView.setOnClickListener {
            listener.stopAllowAttendanceInLiveClass(classes = model)
        }
        val date = GetDateAndTime.getCurrentDate(model.timeStamp)
        val time = GetDateAndTime.getCurrentDate(model.timeStamp)
        holder.classNameLiveClassTextView.text = "${model.className} $date $time"
        holder.locationLiveClassTextView.text = "Location: ${model.location.address}"
        holder.classCodeLiveClassTextView.text = "Class Code ${model.classCode}"

    }


    override fun onDataChanged() {
        super.onDataChanged()
        if (snapshots.isEmpty()) {
            listener.isLiveDataEmpty(true)
        } else {
            listener.isLiveDataEmpty(false)
        }
    }

    override fun onError(e: FirebaseFirestoreException) {
        super.onError(e)
        Log.d(Constant.TEACHER_LOG, "Error on LiveClassesAdapter $e")
    }


}

interface LiveClassListener {
    fun stopAllowAttendanceInLiveClass(classes: Class)
    fun removeClass(classes: Class)
    fun isLiveDataEmpty(value: Boolean)
}