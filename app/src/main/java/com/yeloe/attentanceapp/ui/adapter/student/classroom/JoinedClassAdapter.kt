package com.yeloe.attentanceapp.ui.adapter.student.classroom

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.utils.Constant

class JoinedClassAdapter(
    private val lister: JoinedClassroomListener,
    options: FirestoreRecyclerOptions<JoinClassroom>,
) : FirestoreRecyclerAdapter<JoinClassroom, JoinedClassAdapter.JoinedClassroomHolder>(options) {

    inner class JoinedClassroomHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val classroomLayoutCardView: CardView = itemView.findViewById(R.id.classroomLayoutCardView)
        val classroomNameTextView: TextView = itemView.findViewById(R.id.classroomNameTextView)
        val teacherImageView: ImageView = itemView.findViewById(R.id.teacherImageView)
        val teacherNameTextView: TextView = itemView.findViewById(R.id.teacherNameTextView)
        val teacherCollegeTextView: TextView = itemView.findViewById(R.id.teacherCollegeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JoinedClassroomHolder {
        return JoinedClassroomHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.classroom_layout, parent, false
            )
        )
    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (snapshots.isEmpty()) {
            lister.isClassRoomEmpty(true)
        } else {
            lister.isClassRoomEmpty(false)
        }
    }

    override fun onBindViewHolder(
        holder: JoinedClassroomHolder,
        position: Int,
        model: JoinClassroom
    ) {
        holder.classroomLayoutCardView.setOnClickListener {
            lister.onClickClassroom(model)
        }
        holder.classroomNameTextView.text = model.classroomName
        holder.teacherCollegeTextView.text = model.teacherCollege
        holder.teacherNameTextView.text = "${model.teacherName}(${model.classroomBranch})"
        Glide.with(holder.teacherImageView.context).load(model.teacherImage)
            .into(holder.teacherImageView)
    }

    override fun onError(e: FirebaseFirestoreException) {
        super.onError(e)
        Log.d(Constant.TEACHER_LOG,"Error on JoinedClassAdapter $e")
    }


}

interface JoinedClassroomListener {
    fun onClickClassroom(createClassroom: JoinClassroom)
    fun isClassRoomEmpty(value: Boolean)
}