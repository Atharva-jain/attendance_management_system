package com.yeloe.attentanceapp.view_model

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yeloe.attentanceapp.model.authentication.Face
import com.yeloe.attentanceapp.model.authentication.SignIn
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.student.MarkedAttendance
import com.yeloe.attentanceapp.model.teacher.Attendance
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.model.teacher.CreateClassroom
import com.yeloe.attentanceapp.repo.AttendanceRepository
import com.yeloe.attentanceapp.ui.face_classification.FaceClassifier
import com.yeloe.attentanceapp.ui.face_classification.FaceClassifier.Recognition
import com.yeloe.attentanceapp.utils.Resources
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AttendanceViewModel(
    application: Application, private val attendanceRepository: AttendanceRepository
) : AndroidViewModel(application) {

    val mCreateAccountStatusState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mCreateAccountStatusState
    val mLoginStatusState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mLoginStatusState
    val mForgotPasswordState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mForgotPasswordState
    val mAddStudentUserState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mAddStudentUserState
    val mProfileImageUploadState: MutableLiveData<Resources<String>> =
        attendanceRepository.mProfileImageUploadState
    val mProfileImageProcessState: MutableLiveData<Resources<Long>> =
        attendanceRepository.mProfileImageProcessState
    val mFaceImageUploadState: MutableLiveData<Resources<String>> =
        attendanceRepository.mFaceImageUploadState
    val mFaceImageProcessState: MutableLiveData<Resources<Long>> =
        attendanceRepository.mFaceImageProcessState
    val mEmailIsExistThatTypeState: MutableLiveData<Resources<HashMap<String, Boolean>>> =
        attendanceRepository.mEmailIsExistThatTypeState
    val mCreateClassroomState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mCreateClassroomState
    val mGetCurrentUserState: MutableLiveData<Resources<SignIn>> =
        attendanceRepository.mGetCurrentUserState
    val mCreateClassState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mCreateClassState
    val mDeleteClassroomState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mDeleteClassroomState
    val mDeleteClassState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mDeleteClassState
    val mStopAttendanceClassState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mStopAttendanceClassState
    val mJoinClassroomState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mJoinClassroomState
    val mRemoveStudentJoinClassroomState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mRemoveStudentJoinClassroomState
    val mLeaveJoinClassroomState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mLeaveJoinClassroomState
    val mCheckClassroomJoinState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mCheckClassroomJoinState
    val mFaceVerifyState: MutableLiveData<Boolean> = attendanceRepository.mFaceVerifyState
    val mMarkedAttendanceState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mMarkedAttendanceState
    val mCheckAttendanceMarked: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mCheckAttendanceMarked
    val mGetAttendanceRecordState: MutableLiveData<Resources<Attendance>> =
        attendanceRepository.mGetAttendanceRecordState
    val mRemoveAttendanceState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mRemoveAttendanceState
    val mUpdateClassroomState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mUpdateClassroomState
    val mUpdateTeacherRecordState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mUpdateTeacherRecordState
    val mUpdateStudentRecordState: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mUpdateStudentRecordState
    val mAddFacesState: MutableLiveData<Resources<Boolean>> = attendanceRepository.mAddFacesState
    val mCheckCurrentUserExist: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mCheckCurrentUserExist
    val mDeleteAccountStatus: MutableLiveData<Resources<Boolean>> =
        attendanceRepository.mDeleteAccountStatus
    val mStateOfGettingCurrentData: MutableLiveData<Boolean> = MutableLiveData()
    val mFacesDataState: MutableLiveData<Resources<HashMap<String, FaceClassifier.Recognition>>> =
        attendanceRepository.mFacesDataState


    private var _registeredRecognition = HashMap<String, Recognition>()

    val getRegisteredRecognition get() = _registeredRecognition

    fun setRegisteredRecognition(registered: HashMap<String, Recognition>) {
        _registeredRecognition = registered
    }

    private var _joinedClassroomData: JoinClassroom = JoinClassroom()

    val getJoinedClassroomData get() = _joinedClassroomData

    fun setJoinedClassroomData(joinClassroom: JoinClassroom) {
        _joinedClassroomData = joinClassroom
    }

    private var _createClassroomData: CreateClassroom = CreateClassroom()

    val getCreateClassroomData get() = _createClassroomData

    fun setCreateClassroomData(createClassroom: CreateClassroom) {
        _createClassroomData = createClassroom
    }

    private var _classesListData: ArrayList<Class> = ArrayList()

    val getClassesListData get() = _classesListData

    fun setClassesListData(classesList: ArrayList<Class>) {
        _classesListData = classesList
    }

    private var _attendanceListData: ArrayList<MarkedAttendance> = ArrayList()

    val getAttendanceListData get() = _attendanceListData

    fun setAttendanceListData(attendanceList: ArrayList<MarkedAttendance>) {
        _attendanceListData = attendanceList
    }

    private var _studentData: JoinClassroom = JoinClassroom()

    val getStudentData get() = _studentData


    fun setStudentData(data: JoinClassroom) {
        _studentData = data
    }


    fun addFace(face: Face) {
        viewModelScope.launch {
            attendanceRepository.addFaces(face)
        }
    }

    fun getFaces(uid: String) {
        viewModelScope.launch {
            attendanceRepository.getFaces(uid)
        }
    }

    fun getDataOfAttendance(teacherUid: String, studentUid: String, classroomUid: String) {
        viewModelScope.launch {
            attendanceRepository.getDataOfAttendance(teacherUid, studentUid, classroomUid)
        }
    }

    fun markedAttendance(markedAttendance: MarkedAttendance) {
        viewModelScope.launch {
            attendanceRepository.markedAttendance(markedAttendance)
        }
    }

    fun checkAttendanceMarked(classUid: String, uid: String) {
        viewModelScope.launch {
            attendanceRepository.checkAttendanceMarked(classUid, uid)
        }
    }

    fun removeAttendance(markedAttendance: MarkedAttendance) {
        viewModelScope.launch {
            attendanceRepository.removeAttendance(markedAttendance)
        }
    }

    fun deleteClass(classes: Class) {
        viewModelScope.launch {
            attendanceRepository.deleteClass(classes)
        }
    }

    fun joinClassroom(joinClassroom: JoinClassroom) {
        viewModelScope.launch {
            attendanceRepository.joinClassroom(joinClassroom)
        }
    }

    fun updateClassroomAndJoinClassroom(classroom: CreateClassroom) {
        viewModelScope.launch {
            attendanceRepository.updateClassroomAndJoinClassroom(classroom)
        }
    }

    fun updateSignInAndJoinClassrooms(signIn: SignIn) {
        viewModelScope.launch {
            attendanceRepository.updateSignInAndJoinClassrooms(signIn)
        }
    }

    fun leaveClassroom(joinClassroom: JoinClassroom) {
        viewModelScope.launch {
            attendanceRepository.leaveClassroom(joinClassroom)
        }
    }

    fun checkClassroomIsJoined(uid: String, classroomUid: String) {
        viewModelScope.launch {
            attendanceRepository.checkClassroomIsJoined(uid = uid, classroomUid = classroomUid)
        }
    }

    fun stopAttendanceOnClass(classes: Class) {
        viewModelScope.launch {
            attendanceRepository.stopAttendanceOnClass(classes)
        }
    }

    // this function are use to create account
    fun createAccountFromEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            attendanceRepository.createAccountFromEmailAndPassword(email, password)
        }
    }

    fun createClassroom(createClassroom: CreateClassroom) {
        viewModelScope.launch {
            attendanceRepository.createClassroom(createClassroom)
        }
    }

    fun removeStudentFromClassroom(joinClassroom: JoinClassroom) {
        viewModelScope.launch {
            attendanceRepository.removeStudentFromClassroom(joinClassroom)
        }
    }

    fun deleteClassroom(createClassroom: CreateClassroom) {
        viewModelScope.launch {
            attendanceRepository.deleteClassroom(createClassroom)
        }
    }

    fun createClass(classes: Class) {
        viewModelScope.launch {
            attendanceRepository.createClass(classes)
        }
    }

    fun getTeacherUserData(uid: String) {
        viewModelScope.launch {
            attendanceRepository.getTeacherUserData(uid)
        }
    }

    fun getStudentUserData(uid: String) {
        viewModelScope.launch {
            attendanceRepository.getStudentUserData(uid)
        }
    }

    fun loginThroughEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            attendanceRepository.loginThroughEmailAndPassword(email, password)
        }
    }

    fun sendForgotPasswordLink(email: String) {
        viewModelScope.launch {
            attendanceRepository.sendForgotPasswordLink(email)
        }
    }

    fun addStudentUser(signIn: SignIn) {
        viewModelScope.launch {
            attendanceRepository.addStudentUser(signIn)
        }
    }

    fun updateSignInAndClassrooms(signIn: SignIn) {
        viewModelScope.launch {
            attendanceRepository.updateSignInAndClassrooms(signIn)
        }
    }

    fun addProfileImage(activity: Activity, uri: Uri) {
        viewModelScope.launch {
            attendanceRepository.addProfileImage(activity, uri)
        }
    }

    fun addFaceImage(image: Bitmap) {
        viewModelScope.launch {
            attendanceRepository.addFaceImage(image)
        }
    }

    fun checkDataIsEmailAvailableInCollection(email: String, type: String) {
        viewModelScope.launch {
            attendanceRepository.checkDataIsEmailAvailableInCollection(email, type)
        }
    }

    fun checkAccountExist(email: String) {
        viewModelScope.launch {
            attendanceRepository.checkAccountExist(email)
        }
    }

    fun deleteAccount(email: String, password: String) {
        viewModelScope.launch {
            attendanceRepository.deleteAccount(email, password)
        }
    }

    fun verifyFace(context: Context, studentFaceImage: Bitmap, getFaceImage: Bitmap) {
        GlobalScope.launch {
            attendanceRepository.verifyFace(context, studentFaceImage, getFaceImage)
        }
    }

}