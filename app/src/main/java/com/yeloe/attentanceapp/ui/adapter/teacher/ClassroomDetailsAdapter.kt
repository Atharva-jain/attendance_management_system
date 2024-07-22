package com.yeloe.attentanceapp.ui.adapter.teacher

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
import com.yeloe.attentanceapp.model.teacher.CreateClassroom
import com.yeloe.attentanceapp.utils.Constant

class ClassroomDetailsAdapter(
    private val lister: ClassroomListener,
    options: FirestoreRecyclerOptions<CreateClassroom>,
) : FirestoreRecyclerAdapter<CreateClassroom, ClassroomDetailsAdapter.ClassroomHolder>(options) {

    inner class ClassroomHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val classroomTeacherLayoutCardView: CardView =
            itemView.findViewById(R.id.classroomTeacherLayoutCardView)
        val classroomNameTeacherTextView: TextView =
            itemView.findViewById(R.id.classroomNameTeacherTextView)
        val collegeDetailsTeacherTextView: TextView =
            itemView.findViewById(R.id.collegeDetailsTeacherTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassroomHolder {
        return ClassroomHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.teacher_classroom_layout, parent, false
            )
        )
    }

    override fun onError(e: FirebaseFirestoreException) {
        super.onError(e)
        Log.d(Constant.TEACHER_LOG, "Error on ClassroomDetailsAdapter $e")
    }

    override fun onBindViewHolder(holder: ClassroomHolder, position: Int, model: CreateClassroom) {
        holder.classroomTeacherLayoutCardView.setOnClickListener {
            lister.onClickClassroom(model)
        }
        holder.classroomNameTeacherTextView.text = model.classroomName
        holder.collegeDetailsTeacherTextView.text =
            "${model.classroomCollege}, ${model.classroomBranch}, ${model.classroomYear}, ${model.classroomSemester}"
    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (snapshots.isEmpty()) {
            lister.isClassRoomEmpty(true)
        } else {
            lister.isClassRoomEmpty(false)
        }
    }

}

interface ClassroomListener {
    fun onClickClassroom(createClassroom: CreateClassroom)
    fun isClassRoomEmpty(value: Boolean)
}