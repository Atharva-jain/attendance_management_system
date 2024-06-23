package com.yeloe.attentanceapp.ui.adapter.student.classroom

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.model.teacher.CreateClassroom
import com.yeloe.attentanceapp.utils.Constant


class JoinClassroomAdapter(
    private val lister: JoinClassroomListener,
    options: FirestoreRecyclerOptions<CreateClassroom>,
) : FirestoreRecyclerAdapter<CreateClassroom, JoinClassroomAdapter.JoinClassroomHolder>(options) {

    inner class JoinClassroomHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val joinClassroomNameTextView: TextView =
            itemView.findViewById(R.id.joinClassroomNameTextView)
        val joinClassroomTeacherNameTextView: TextView =
            itemView.findViewById(R.id.joinClassroomTeacherNameTextView)
        val joinClassroomCollegeTextView: TextView =
            itemView.findViewById(R.id.joinClassroomCollegeTextView)
        val joinClassroomYearTextView: TextView =
            itemView.findViewById(R.id.joinClassroomYearTextView)
        val joinClassroomBranchTextView: TextView =
            itemView.findViewById(R.id.joinClassroomBranchTextView)
        val joinClassroomSemesterTextView: TextView =
            itemView.findViewById(R.id.joinClassroomSemesterTextView)
        val joinClassroomTeacherImageImageView: ImageView =
            itemView.findViewById(R.id.joinClassroomTeacherImageImageView)
        val joinClassroomButton: Button = itemView.findViewById(R.id.joinClassroomButton)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JoinClassroomHolder {
        return JoinClassroomHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.join_classroom_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: JoinClassroomHolder,
        position: Int,
        model: CreateClassroom
    ) {
        holder.joinClassroomBranchTextView.text = model.classroomBranch
        holder.joinClassroomCollegeTextView.text = model.classroomCollege
        holder.joinClassroomNameTextView.text = model.classroomName
        holder.joinClassroomSemesterTextView.text = model.classroomSemester
        holder.joinClassroomTeacherNameTextView.text = model.teacherName
        holder.joinClassroomYearTextView.text = model.classroomYear
        Glide.with(holder.joinClassroomTeacherImageImageView.context).load(model.teacherImage)
            .into(holder.joinClassroomTeacherImageImageView)
        holder.joinClassroomButton.setOnClickListener {
            lister.onClickJoinedClassroom(model)
        }
    }


    override fun onDataChanged() {
        super.onDataChanged()
        if (snapshots.isEmpty()) {
            lister.isClassRoomJoinedEmpty(true)
        } else {
            lister.isClassRoomJoinedEmpty(false)
        }
    }

    override fun onError(e: FirebaseFirestoreException) {
        super.onError(e)
        Log.d(Constant.TEACHER_LOG,"Error on JoinClassroomAdapter $e")
    }


}

interface JoinClassroomListener {
    fun onClickJoinedClassroom(createClassroom: CreateClassroom)
    fun isClassRoomJoinedEmpty(value: Boolean)
}