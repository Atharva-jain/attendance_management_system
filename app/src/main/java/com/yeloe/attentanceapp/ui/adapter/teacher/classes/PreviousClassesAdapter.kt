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

class PreviousClassesAdapter(
    private val lister: PreviousClassListener,
    options: FirestoreRecyclerOptions<Class>,
) : FirestoreRecyclerAdapter<Class, PreviousClassesAdapter.PreviousClassHolder>(options) {

    inner class PreviousClassHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val previousLayoutCardView: MaterialCardView =
            itemView.findViewById(R.id.previousLayoutCardView)
        val datePreviousClassTextView: TextView =
            itemView.findViewById(R.id.datePreviousClassTextView)
        val timePreviousClassTextView: TextView =
            itemView.findViewById(R.id.timePreviousClassTextView)
        val locationPreviousClassTextView: TextView =
            itemView.findViewById(R.id.locationPreviousClassTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviousClassHolder {
        return PreviousClassHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.teacher_previous_classes_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: PreviousClassHolder, position: Int, model: Class) {
        holder.datePreviousClassTextView.text = GetDateAndTime.getCurrentDate(model.timeStamp)
        holder.timePreviousClassTextView.text = GetDateAndTime.getCurrentTime(model.timeStamp)
        holder.locationPreviousClassTextView.text = model.location.address
        holder.previousLayoutCardView.setOnClickListener {
            lister.previousClassClicked(classes = model)
        }
    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (snapshots.isEmpty()) {
            lister.isPreviousDataEmpty(true)
        } else {
            lister.isPreviousDataEmpty(false)
        }
    }

    override fun onError(e: FirebaseFirestoreException) {
        super.onError(e)
        Log.d(Constant.TEACHER_LOG,"Error on PreviousClassesAdapter $e")
    }

}

interface PreviousClassListener {
    fun previousClassClicked(classes: Class)
    fun isPreviousDataEmpty(value: Boolean)
}