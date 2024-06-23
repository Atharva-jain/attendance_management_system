package com.yeloe.attentanceapp.ui.adapter.teacher.student_list

import android.graphics.Paint.Join
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestoreException
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.LiveClassListener
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.LiveClassesAdapter
import com.yeloe.attentanceapp.utils.Constant

class StudentClassroomJoinedListAdapter(
    private val lister: OnJoinedStudentListListener,
    options: FirestoreRecyclerOptions<JoinClassroom>,
) : FirestoreRecyclerAdapter<JoinClassroom, StudentClassroomJoinedListAdapter.StudentClassroomJoinedListHolder>(
    options
) {


    inner class StudentClassroomJoinedListHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val studentListCardView: MaterialCardView = itemView.findViewById(R.id.studentListCardView)
        val studentProfileListImageView: ImageView =
            itemView.findViewById(R.id.studentProfileListImageView)
        val studentNameListTextView: TextView = itemView.findViewById(R.id.studentNameListTextView)
        val studentDetailsListTextView: TextView =
            itemView.findViewById(R.id.studentDetailsListTextView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): StudentClassroomJoinedListHolder {
        return StudentClassroomJoinedListHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.student_list_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: StudentClassroomJoinedListHolder, position: Int, model: JoinClassroom
    ) {
        holder.studentNameListTextView.text = model.studentName
        holder.studentDetailsListTextView.text =
            "${model.studentCollege}(${model.studentBranch}:${model.studentYear}:${model.studentSemester})"
        Glide.with(holder.studentProfileListImageView.context).load(model.studentProfileImage)
            .into(holder.studentProfileListImageView)
        holder.studentListCardView.setOnClickListener {
            lister.studentClick(model)
        }
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
        Log.d(Constant.TEACHER_LOG, "Error on LiveClassesAdapter $e")
    }

}

interface OnJoinedStudentListListener {
    fun studentClick(joinClassroom: JoinClassroom)
    fun isLiveDataEmpty(value: Boolean)
}