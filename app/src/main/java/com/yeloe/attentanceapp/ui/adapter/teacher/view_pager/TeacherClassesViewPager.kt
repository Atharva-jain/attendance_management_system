package com.yeloe.attentanceapp.ui.adapter.teacher.view_pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yeloe.attentanceapp.ui.fragment.teacher.classes.ClassStudentFragment
import com.yeloe.attentanceapp.ui.fragment.teacher.classes.ClassesFragment
import com.yeloe.attentanceapp.utils.Constant

class TeacherClassesViewPager(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return Constant.CLASSES_TAB_VALUE
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ClassesFragment()
            }

            1 -> {
                ClassStudentFragment()
            }

            else -> {
                ClassesFragment()
            }
        }
    }


}