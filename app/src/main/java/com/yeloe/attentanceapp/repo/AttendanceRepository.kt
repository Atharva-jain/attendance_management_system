package com.yeloe.attentanceapp.repo


import android.R.attr
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.yeloe.attentanceapp.ml.OptimizeFaceVerificationModel
import com.yeloe.attentanceapp.model.authentication.Face
import com.yeloe.attentanceapp.model.authentication.SignIn
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.student.MarkedAttendance
import com.yeloe.attentanceapp.model.teacher.Attendance
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.model.teacher.CreateClassroom
import com.yeloe.attentanceapp.ui.face_classification.FaceClassifier
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.EmBeddingConverter
import com.yeloe.attentanceapp.utils.GetImageExtension
import com.yeloe.attentanceapp.utils.Resources
import kotlinx.coroutines.tasks.await
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream


class AttendanceRepository {


    private var imageProcessor =
        ImageProcessor.Builder().add(ResizeOp(64, 64, ResizeOp.ResizeMethod.BILINEAR))
            .add(TransformToGrayscaleOp()).build()

    private var mAuth: FirebaseAuth = Firebase.auth
    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mStudentCollection =
        mFirebaseFireStoreInstance.collection(Constant.studentUserCollection)
    private val mTeacherCollection =
        mFirebaseFireStoreInstance.collection(Constant.teacherUserCollection)
    private val mTeacherClassroomCollection =
        mFirebaseFireStoreInstance.collection(Constant.teacherClassRoomCollection)
    private val mClassesCollection =
        mFirebaseFireStoreInstance.collection(Constant.classesCollection)
    private val mJoinClassroomCollection =
        mFirebaseFireStoreInstance.collection(Constant.joinClassroomCollection)
    private val mAttendanceCollection = mFirebaseFireStoreInstance.collection(Constant.attendance)
    private val mFaceCollection = mFirebaseFireStoreInstance.collection(Constant.faces)
    private val mFirebaseCloudInstance = FirebaseStorage.getInstance().reference

    val mCreateAccountStatusState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mLoginStatusState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mForgotPasswordState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mAddStudentUserState: MutableLiveData<Resources<Boolean>> =
        MutableLiveData(Resources.Completed())
    val mProfileImageUploadState: MutableLiveData<Resources<String>> = MutableLiveData()
    val mProfileImageProcessState: MutableLiveData<Resources<Long>> = MutableLiveData()
    val mFaceImageUploadState: MutableLiveData<Resources<String>> = MutableLiveData()
    val mFaceImageProcessState: MutableLiveData<Resources<Long>> = MutableLiveData()
    val mEmailIsExistThatTypeState: MutableLiveData<Resources<HashMap<String, Boolean>>> =
        MutableLiveData()
    val mCreateClassroomState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mDeleteClassroomState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mCreateClassState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mGetCurrentUserState: MutableLiveData<Resources<SignIn>> = MutableLiveData()
    val mCheckCurrentUserExist: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mDeleteAccountStatus: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mDeleteClassState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mStopAttendanceClassState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mJoinClassroomState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mRemoveStudentJoinClassroomState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mLeaveJoinClassroomState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mCheckClassroomJoinState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mFaceVerifyState: MutableLiveData<Boolean> = MutableLiveData()
    val mMarkedAttendanceState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mCheckAttendanceMarked: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mRemoveAttendanceState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mGetAttendanceRecordState: MutableLiveData<Resources<Attendance>> = MutableLiveData()
    val mUpdateClassroomState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mUpdateTeacherRecordState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mUpdateStudentRecordState: MutableLiveData<Resources<Boolean>> = MutableLiveData()
    val mFacesDataState: MutableLiveData<Resources<HashMap<String, FaceClassifier.Recognition>>> =
        MutableLiveData()
    val mAddFacesState: MutableLiveData<Resources<Boolean>> = MutableLiveData()

    suspend fun updateSignInAndClassrooms(signIn: SignIn) {
        val signInRef = mTeacherCollection.document(signIn.uid)
        val classroomRef = mTeacherClassroomCollection
        val joinClassroomRef = mJoinClassroomCollection

        mUpdateTeacherRecordState.postValue(Resources.Loading())
        classroomRef.whereEqualTo(Constant.UID, signIn.uid).get()
            .addOnSuccessListener { classroomSnapshot ->
                joinClassroomRef.whereEqualTo(Constant.TEACHER_UID, signIn.uid).get()
                    .addOnSuccessListener { joinClassroomSnapshot ->
                        mFirebaseFireStoreInstance.runTransaction { transaction ->
                            // Update SignIn data
                            transaction.set(signInRef, signIn)

                            // Update Classroom data
                            classroomSnapshot.documents.forEach { classroomDoc ->
                                val classroomData =
                                    classroomDoc.toObject(CreateClassroom::class.java)
                                classroomData?.teacherImage = signIn.name
                                classroomData?.teacherCollege = signIn.college
                                classroomData?.teacherImage = signIn.profileImage
                                if (classroomData != null) {
                                    transaction.set(classroomDoc.reference, classroomData)
                                } else {
                                    mUpdateTeacherRecordState.postValue(Resources.Success(false))
                                }
                            }

                            // Update JoinClassroom data
                            joinClassroomSnapshot.documents.forEach { joinClassroomDoc ->
                                val joinClassroomData =
                                    joinClassroomDoc.toObject(JoinClassroom::class.java)
                                joinClassroomData?.teacherName = signIn.name
                                joinClassroomData?.teacherCollege = signIn.college
                                joinClassroomData?.teacherImage = signIn.profileImage
                                if (joinClassroomData != null) {
                                    transaction.set(joinClassroomDoc.reference, joinClassroomData)
                                } else {
                                    mUpdateTeacherRecordState.postValue(Resources.Success(false))
                                }
                            }
                            mUpdateTeacherRecordState.postValue(Resources.Success(true))
                            null
                        }.addOnSuccessListener {
                            // Transaction successful
                            // Handle success if needed
                            mUpdateTeacherRecordState.postValue(Resources.Success(true))
                        }.addOnFailureListener { e ->
                            // Transaction failed
                            // Handle failure if needed
                            mUpdateTeacherRecordState.postValue(Resources.Error(e.message.toString()))
                        }
                    }.addOnFailureListener { e ->
                        mUpdateTeacherRecordState.postValue(Resources.Error(e.message.toString()))
                    }
            }.addOnFailureListener { e ->
                mUpdateTeacherRecordState.postValue(Resources.Error(e.message.toString()))
            }.await()
    }

    suspend fun updateSignInAndJoinClassrooms(signIn: SignIn) {

        val joinClassroomRef = mJoinClassroomCollection.whereEqualTo(Constant.UID, signIn.uid)

        joinClassroomRef.get().addOnSuccessListener { joinClassroomSnapshot ->
            mFirebaseFireStoreInstance.runTransaction { transaction ->
                // Update Classroom
                val classroomRef = mStudentCollection.document(signIn.uid)
                transaction.set(classroomRef, signIn)

                // Update JoinClassroom
                for (doc in joinClassroomSnapshot.documents) {
                    val joinClassroomReference = mJoinClassroomCollection.document(doc.id)
                    val joinClassroomData = doc.toObject(JoinClassroom::class.java)
                    joinClassroomData?.studentProfileImage = signIn.profileImage
                    joinClassroomData?.studentSemester = signIn.semester
                    joinClassroomData?.studentBranch = signIn.branch
                    joinClassroomData?.studentFaceImage = signIn.faceImage
                    joinClassroomData?.studentYear = signIn.year
                    joinClassroomData?.studentSemester = signIn.semester
                    joinClassroomData?.studentCollege = signIn.college
                    joinClassroomData?.studentName = signIn.name

                    if (joinClassroomData != null) {
                        transaction.set(joinClassroomReference, joinClassroomData)
                    } else {
                        mUpdateStudentRecordState.postValue(Resources.Success(false))
                    }
                }
                null
            }.addOnSuccessListener {
                mUpdateStudentRecordState.postValue(Resources.Success(true))
                // Handle success if needed
            }.addOnFailureListener { e ->
                // Transaction failed
                mUpdateStudentRecordState.postValue(Resources.Error(e.message.toString()))
                // Handle failure if needed
            }
        }.addOnFailureListener { e ->
            // Handle failure to fetch documents
            mUpdateStudentRecordState.postValue(Resources.Error(e.message.toString()))
        }.await()

    }

    suspend fun updateClassroomAndJoinClassroom(classroom: CreateClassroom) {
        val joinClassroomQuery =
            mJoinClassroomCollection.whereEqualTo(Constant.CLASSROOM_UID, classroom.classroomUid)
        mUpdateClassroomState.postValue(Resources.Loading())

        joinClassroomQuery.get().addOnSuccessListener { joinClassroomSnapshot ->
            mFirebaseFireStoreInstance.runTransaction { transaction ->
                // Update Classroom
                val classroomRef = mTeacherClassroomCollection.document(classroom.classroomUid)
                transaction.set(classroomRef, classroom)

                // Update JoinClassroom
                for (doc in joinClassroomSnapshot.documents) {
                    val joinClassroomRef = mJoinClassroomCollection.document(doc.id)
                    val joinClassroomData = doc.toObject(JoinClassroom::class.java)
                    joinClassroomData?.classroomName = classroom.classroomName
                    joinClassroomData?.classroomCollege = classroom.classroomCollege
                    joinClassroomData?.classroomBranch = classroom.classroomBranch
                    joinClassroomData?.classroomYear = classroom.classroomYear
                    joinClassroomData?.classroomSemester = classroom.classroomSemester
                    joinClassroomData?.keywords = classroom.keywords
                    if (joinClassroomData != null) {
                        transaction.set(joinClassroomRef, joinClassroomData)
                    } else {
                        mUpdateClassroomState.postValue(Resources.Success(false))
                    }
                }
                null
            }.addOnSuccessListener {
                mUpdateClassroomState.postValue(Resources.Success(true))
                // Handle success if needed
            }.addOnFailureListener { e ->
                // Transaction failed
                mUpdateClassroomState.postValue(Resources.Error(e.message.toString()))
                // Handle failure if needed
            }
        }.addOnFailureListener { e ->
            // Handle failure to fetch documents
            mUpdateClassroomState.postValue(Resources.Error(e.message.toString()))
        }.await()
    }

    suspend fun addFaces(face: Face) {
        try {
            mAddFacesState.postValue(Resources.Loading())
            mFaceCollection.document(face.name).set(face).addOnSuccessListener {
                mAddFacesState.postValue(Resources.Success(true))
            }.addOnFailureListener {
                mAddFacesState.postValue(Resources.Error(it.message.toString()))
            }.await()

        } catch (e: Exception) {
            mAddFacesState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun getFaces(uid: String) {
        try {
            val documentFacesSnapshot =
                mFaceCollection.whereEqualTo(Constant.NAME, uid).get().await().documents.toList()
            val facesHash = convertDocumentSnapshotToFaces(documentFacesSnapshot)
            mFacesDataState.postValue(Resources.Success(facesHash))

        } catch (e: Exception) {
            mFacesDataState.postValue(Resources.Error(e.message.toString()))
            Log.d(Constant.REPOSITORY_LOG, "Error on get Faces $e")
        }

    }

    suspend fun markedAttendance(markedAttendance: MarkedAttendance) {
        try {
            mAttendanceCollection.document(markedAttendance.attendanceUid).set(markedAttendance)
                .addOnSuccessListener {
                    mMarkedAttendanceState.postValue(Resources.Success(true))
                }.addOnFailureListener {
                    mMarkedAttendanceState.postValue(Resources.Error(it.message.toString()))
                }.await()

        } catch (e: Exception) {
            mMarkedAttendanceState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun checkAttendanceMarked(classUid: String, uid: String) {
        try {
            mCheckAttendanceMarked.postValue(Resources.Loading())
            Log.d(Constant.CREATE_ACCOUNT_LOG, "Attendance section")
            val documentSnapshot = mAttendanceCollection
                .whereEqualTo(Constant.CLASS_UIS, classUid)
                .whereEqualTo(Constant.UID, uid)
                .get()
                .await()
                .documents
                .toList()
            val list = convertDocumentSnapshotToAttendance(documentSnapshot)
            if (list.isNotEmpty()) {
                mCheckAttendanceMarked.postValue(Resources.Success(false))
            } else {
                mCheckAttendanceMarked.postValue(Resources.Success(true))
            }
        } catch (e: Exception) {
            mCheckAttendanceMarked.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun removeAttendance(markedAttendance: MarkedAttendance) {
        try {
            mAttendanceCollection.document(markedAttendance.attendanceUid).delete()
                .addOnSuccessListener {
                    mRemoveAttendanceState.postValue(Resources.Success(true))
                }.addOnFailureListener {
                    mRemoveAttendanceState.postValue(Resources.Error(it.message.toString()))
                }.await()
        } catch (e: Exception) {
            mRemoveAttendanceState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun getDataOfAttendance(teacherUid: String, studentUid: String, classroomUid: String) {
        try {
            mGetAttendanceRecordState.postValue(Resources.Loading())
            val classesList = ArrayList<Class>()
            val attendancesList = ArrayList<MarkedAttendance>()
            val documentClassSnapshot = mClassesCollection.whereEqualTo(Constant.UID, teacherUid)
                .whereEqualTo(Constant.CLASSROOM_UID, classroomUid)
                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING).get()
                .await().documents.toList()
            val documentAttendanceSnapshot =
                mAttendanceCollection.whereEqualTo(Constant.UID, studentUid)
                    .whereEqualTo(Constant.CLASSROOM_UID, classroomUid)
                    .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING).get()
                    .await().documents.toList()
            val classList = convertDocumentSnapshotToClass(documentClassSnapshot)
            val attendanceList = convertDocumentSnapshotToAttendance(documentAttendanceSnapshot)
            classList.forEach { classes ->
                if (classes != null) {
                    classesList.add(classes)
                }
            }
            attendanceList.forEach { attendance ->
                if (attendance != null) {
                    attendancesList.add(attendance)
                }
            }
            val attendance = Attendance(attendancesList, classesList)
            mGetAttendanceRecordState.postValue(Resources.Success(attendance))
        } catch (e: Exception) {
            mGetAttendanceRecordState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun checkDataIsEmailAvailableInCollection(email: String, type: String) {
        val resultMap: HashMap<String, Boolean> = HashMap<String, Boolean>()
        try {
            if (type == Constant.TEACHER_LOGIN) {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Teacher section")
                val documentSnapshot = mTeacherCollection.whereEqualTo(Constant.EMAIL, email).get()
                    .await().documents.toList()
                val list = convertDocumentSnapshotToUser(documentSnapshot)
                if (list.isNotEmpty()) {
                    resultMap[type] = true
                    mEmailIsExistThatTypeState.postValue(Resources.Success(resultMap))
                } else {
//                    resultMap[type] = false
//                    mEmailIsExistThatTypeState.postValue(Resources.Success(resultMap))
                    Log.d(Constant.CREATE_ACCOUNT_LOG, "Student section from teachers")
                    val documentSnapshotOfStudent =
                        mStudentCollection.whereEqualTo(Constant.EMAIL, email).get()
                            .await().documents.toList()
                    val list = convertDocumentSnapshotToUser(documentSnapshotOfStudent)
                    if (list.isNotEmpty()) {
                        resultMap[Constant.STUDENT_LOGIN_REVISED] = false
                        mEmailIsExistThatTypeState.postValue(Resources.Success(resultMap))
                    } else {
                        resultMap[Constant.NOBODY] = false
                        mEmailIsExistThatTypeState.postValue(Resources.Success(resultMap))
                    }
                }
            } else if (type == Constant.STUDENT_LOGIN) {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Student section")
                val documentSnapshot = mStudentCollection.whereEqualTo(Constant.EMAIL, email).get()
                    .await().documents.toList()
                val list = convertDocumentSnapshotToUser(documentSnapshot)
                if (list.isNotEmpty()) {
                    resultMap[type] = true
                    mEmailIsExistThatTypeState.postValue(Resources.Success(resultMap))
                } else {
//                    resultMap[type] = false
//                    mEmailIsExistThatTypeState.postValue(Resources.Success(resultMap))
                    Log.d(Constant.CREATE_ACCOUNT_LOG, "Teacher section")
                    val documentSnapshotOfTeacher =
                        mTeacherCollection.whereEqualTo(Constant.EMAIL, email).get()
                            .await().documents.toList()
                    val list = convertDocumentSnapshotToUser(documentSnapshotOfTeacher)
                    if (list.isNotEmpty()) {
                        resultMap[Constant.TEACHER_LOGIN_REVISED] = false
                        mEmailIsExistThatTypeState.postValue(Resources.Success(resultMap))
                    } else {
                        resultMap[Constant.NOBODY] = false
                        mEmailIsExistThatTypeState.postValue(Resources.Success(resultMap))
                    }
                }
            } else {
                resultMap[type] = false
                mEmailIsExistThatTypeState.postValue(Resources.Success(resultMap))
            }

        } catch (e: Exception) {
            resultMap[Constant.NOBODY] = false
            mEmailIsExistThatTypeState.postValue(Resources.Success(resultMap))
        }
    }

    suspend fun addStudentUser(signIn: SignIn) {
        try {
            if (signIn.type == Constant.TEACHER_LOGIN) {
                mTeacherCollection.document(signIn.uid).set(signIn).addOnSuccessListener {
                    mAddStudentUserState.postValue(Resources.Success(true))
                }.addOnFailureListener {
                    mAddStudentUserState.postValue(Resources.Error(it.message.toString()))
                }.await()
            } else if (signIn.type == Constant.STUDENT_LOGIN) {
                mStudentCollection.document(signIn.uid).set(signIn).addOnSuccessListener {
                    mAddStudentUserState.postValue(Resources.Success(true))
                }.addOnFailureListener {
                    mAddStudentUserState.postValue(Resources.Error(it.message.toString()))
                }.await()
            }
        } catch (e: Exception) {
            mAddStudentUserState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun createClassroom(createClassroom: CreateClassroom) {
        try {
            mTeacherClassroomCollection.document(createClassroom.classroomUid).set(createClassroom)
                .addOnSuccessListener {
                    mCreateClassroomState.postValue(Resources.Success(true))
                }.addOnFailureListener {
                    mCreateClassroomState.postValue(Resources.Error(it.message.toString()))
                }.await()

        } catch (e: Exception) {
            mCreateClassroomState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun removeStudentFromClassroom(joinClassroom: JoinClassroom) {
        try {
            mJoinClassroomCollection.document(joinClassroom.joinClassRoomUid).delete()
                .addOnSuccessListener {
                    mRemoveStudentJoinClassroomState.postValue(Resources.Success(true))
                }.addOnFailureListener {
                    mRemoveStudentJoinClassroomState.postValue(Resources.Error(it.message.toString()))
                }.await()

        } catch (e: Exception) {
            mRemoveStudentJoinClassroomState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun deleteClassroom(createClassroom: CreateClassroom) {
        try {
            mTeacherClassroomCollection.document(createClassroom.classroomUid).delete()
                .addOnSuccessListener {
                    mDeleteClassroomState.postValue(Resources.Success(true))
                }.addOnFailureListener {
                    mDeleteClassroomState.postValue(Resources.Error(it.message.toString()))
                }.await()

        } catch (e: Exception) {
            mDeleteClassroomState.postValue(Resources.Error(e.message.toString()))
        }

    }

    suspend fun createClass(classes: Class) {
        try {
            mClassesCollection.document(classes.classUid).set(classes).addOnSuccessListener {
                mCreateClassState.postValue(Resources.Success(true))
            }.addOnFailureListener {
                mCreateClassState.postValue(Resources.Error(it.message.toString()))
            }.await()

        } catch (e: Exception) {
            mCreateClassState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun joinClassroom(joinClassroom: JoinClassroom) {
        try {
            mJoinClassroomCollection.document(joinClassroom.joinClassRoomUid).set(joinClassroom)
                .addOnSuccessListener {
                    mJoinClassroomState.postValue(Resources.Success(true))
                }.addOnFailureListener {
                    mJoinClassroomState.postValue(Resources.Error(it.message.toString()))
                }.await()

        } catch (e: Exception) {
            mJoinClassroomState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun leaveClassroom(joinClassroom: JoinClassroom) {
        try {
            mJoinClassroomCollection.document(joinClassroom.joinClassRoomUid).delete()
                .addOnSuccessListener {
                    mLeaveJoinClassroomState.postValue(Resources.Success(true))
                }.addOnFailureListener {
                    mLeaveJoinClassroomState.postValue(Resources.Error(it.message.toString()))
                }.await()

        } catch (e: Exception) {
            mLeaveJoinClassroomState.postValue(Resources.Error(e.message.toString()))
        }
    }


    suspend fun checkClassroomIsJoined(uid: String, classroomUid: String) {
        try {
            val documentSnapshot = mJoinClassroomCollection.whereEqualTo(Constant.UID, uid)
                .whereEqualTo(Constant.CLASSROOM_UID, classroomUid).get().await().documents.toList()
            val list = convertDocumentSnapshotToJoinedClassroom(documentSnapshot)
            if (list.isNotEmpty()) {
                mCheckClassroomJoinState.postValue(Resources.Success(false))
            } else {
                mCheckClassroomJoinState.postValue(Resources.Success(true))
            }
        } catch (e: Exception) {
            mCheckClassroomJoinState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun deleteClass(classes: Class) {
        try {
            mClassesCollection.document(classes.classUid).delete().addOnSuccessListener {
                mDeleteClassState.postValue(Resources.Success(true))
            }.addOnFailureListener {
                mDeleteClassState.postValue(Resources.Error(it.message.toString()))
            }.await()

            mJoinClassroomCollection.document(classes.classUid).delete().await()
        } catch (e: Exception) {
            mDeleteClassState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun stopAttendanceOnClass(classes: Class) {
        try {
            mClassesCollection.document(classes.classUid).set(classes).addOnSuccessListener {
                mStopAttendanceClassState.postValue(Resources.Success(true))
            }.addOnFailureListener {
                mStopAttendanceClassState.postValue(Resources.Error(it.message.toString()))
            }.await()

        } catch (e: Exception) {
            mStopAttendanceClassState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun checkAccountExist(email: String) {
        try {
            val documentSnapshotTeacher =
                mTeacherCollection.whereEqualTo(Constant.EMAIL, email).get()
                    .await().documents.toList()
            val documentSnapshotStudent =
                mStudentCollection.whereEqualTo(Constant.EMAIL, email).get()
                    .await().documents.toList()
            val teacherList = convertDocumentSnapshotToUser(documentSnapshotTeacher)
            val studentList = convertDocumentSnapshotToUser(documentSnapshotStudent)
            if (teacherList.isNotEmpty()) {
                mCheckCurrentUserExist.postValue(Resources.Success(false))
            } else if (studentList.isNotEmpty()) {
                mCheckCurrentUserExist.postValue(Resources.Success(false))
            } else {
                mCheckCurrentUserExist.postValue(Resources.Success(true))
            }
        } catch (e: Exception) {
            mCheckCurrentUserExist.postValue(Resources.Error(Constant.ACCOUNT_ALREADY_EXIST_ERROR))
        }
    }

    suspend fun getTeacherUserData(uid: String) {
        try {
            val documentSnapshot =
                mTeacherCollection.whereEqualTo(Constant.UID, uid).get().await().documents.toList()
            val list = convertDocumentSnapshotToUser(documentSnapshot)
            list.forEach { user ->
                if (user != null) {
                    mGetCurrentUserState.postValue(Resources.Success(user))
                }
            }
        } catch (e: Exception) {
            mGetCurrentUserState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun getStudentUserData(uid: String) {
        try {
            val documentSnapshot =
                mStudentCollection.whereEqualTo(Constant.UID, uid).get().await().documents.toList()
            val list = convertDocumentSnapshotToUser(documentSnapshot)
            list.forEach { user ->
                if (user != null) {
                    mGetCurrentUserState.postValue(Resources.Success(user))
                }
            }
        } catch (e: Exception) {
            mGetCurrentUserState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun getUserData(type: String, uid: String) {
        try {
            if (type == Constant.TEACHER_LOGIN) {
                val documentSnapshot = mTeacherCollection.whereEqualTo(Constant.UID, uid).get()
                    .await().documents.toList()
                val list = convertDocumentSnapshotToUser(documentSnapshot)
                list.forEach { user ->
                    if (user != null) {
                        mGetCurrentUserState.postValue(Resources.Success(user))
                    }
                }
            } else if (type == Constant.STUDENT_LOGIN) {
                val documentSnapshot = mStudentCollection.whereEqualTo(Constant.UID, uid).get()
                    .await().documents.toList()
                val list = convertDocumentSnapshotToUser(documentSnapshot)
                list.forEach { user ->
                    if (user != null) {
                        mGetCurrentUserState.postValue(Resources.Success(user))
                    }
                }
            }
        } catch (e: Exception) {
            mGetCurrentUserState.postValue(Resources.Error(e.message.toString()))
        }
    }

    // this function are use to create account
    suspend fun createAccountFromEmailAndPassword(email: String, password: String) {
        try {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    mCreateAccountStatusState.postValue(Resources.Success(true))
                } else {
                    mCreateAccountStatusState.postValue(Resources.Success(false))
                }
            }.addOnFailureListener {
                mCreateAccountStatusState.postValue(Resources.Error(it.message.toString()))
            }.await()

        } catch (e: Exception) {
            mCreateAccountStatusState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun loginThroughEmailAndPassword(email: String, password: String) {
        try {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    mLoginStatusState.postValue(Resources.Success(true))
                } else {
                    mLoginStatusState.postValue(Resources.Success(false))
                }
            }.addOnFailureListener {
                mLoginStatusState.postValue(Resources.Error(it.message.toString()))
            }.await()
        } catch (e: Exception) {
            mLoginStatusState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun sendForgotPasswordLink(email: String) {
        try {
            mAuth.sendPasswordResetEmail(email).addOnSuccessListener {
                mForgotPasswordState.postValue(Resources.Success(true))
            }.addOnFailureListener {
                mForgotPasswordState.postValue(Resources.Error(it.message.toString()))
            }.await()
        } catch (e: Exception) {
            mForgotPasswordState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun deleteAccount(email: String, password: String) {
        try {
            mDeleteAccountStatus.postValue(Resources.Loading())
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                val user = FirebaseAuth.getInstance().currentUser
                user?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(Constant.REPOSITORY_LOG, "User account deleted.")
                        mDeleteAccountStatus.postValue(Resources.Success(true))
                    } else {
                        mDeleteAccountStatus.postValue(Resources.Success(false))
                    }
                }?.addOnFailureListener {
                    mDeleteAccountStatus.postValue(Resources.Error(it.message.toString()))
                }
            }.addOnFailureListener {
                mDeleteAccountStatus.postValue(Resources.Error(it.message.toString()))
            }.await()
        } catch (e: Exception) {
            mDeleteAccountStatus.postValue(Resources.Error(e.message.toString()))
        }

    }

    suspend fun addProfileImage(activity: Activity, uri: Uri) {
        try {
            val imageExtension = GetImageExtension.getFileExtension(activity, uri)
            if (imageExtension != null) {
                val fileRef: StorageReference =
                    mFirebaseCloudInstance.child("${System.currentTimeMillis()}.$imageExtension")
                fileRef.putFile(uri).addOnSuccessListener { task ->
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        val url = uri.toString()
                        mProfileImageUploadState.postValue(Resources.Success(url))
                    }.addOnFailureListener {
                        mProfileImageUploadState.postValue(Resources.Error(it.message.toString()))
                    }
                }.addOnProgressListener { task ->
                    val progress = (100 * task.bytesTransferred) / task.totalByteCount
                    mProfileImageProcessState.postValue(Resources.Success(progress))
                }.addOnFailureListener {
                    Log.d(Constant.REPOSITORY_LOG, "error on image  $it")
                }.await()
            } else {
                mProfileImageUploadState.postValue(Resources.Error("Image is not in suitable format.."))
            }
        } catch (e: Exception) {
            mProfileImageUploadState.postValue(Resources.Error(e.message.toString()))
        }
    }

    suspend fun addFaceImage(image: Bitmap) {
        try {
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val fileRef: StorageReference =
                mFirebaseCloudInstance.child("${System.currentTimeMillis()}.JPEG")
            fileRef.putBytes(data).addOnFailureListener {
                mFaceImageUploadState.postValue(Resources.Error(it.message.toString()))
            }.addOnSuccessListener { taskSnapshot ->
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val url = uri.toString()
                    mFaceImageUploadState.postValue(Resources.Success(url))
                }.addOnFailureListener {
                    mFaceImageUploadState.postValue(Resources.Error(it.message.toString()))
                }
            }.addOnProgressListener { task ->
                val progress = (100 * task.bytesTransferred) / task.totalByteCount
                mFaceImageProcessState.postValue(Resources.Success(progress))
            }.await()
        } catch (e: Exception) {
            mFaceImageUploadState.postValue(Resources.Error(e.message.toString()))
        }

    }

    // this function are use to check email are valid or not
    fun sendEmailVerification() {
        val user = mAuth.currentUser
        user?.sendEmailVerification()
    }

    private fun getUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun verifyFace(context: Context, studentFaceImage: Bitmap, getFaceImage: Bitmap) {
        try {
            var studentTensorImage = TensorImage(DataType.FLOAT32)
            studentTensorImage.load(studentFaceImage)
            studentTensorImage = imageProcessor.process(studentTensorImage)

            var currentTensorImage = TensorImage(DataType.FLOAT32)
            currentTensorImage.load(getFaceImage)
            currentTensorImage = imageProcessor.process(currentTensorImage)

            val model = OptimizeFaceVerificationModel.newInstance(context)

            // Create inputs for the model
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 64, 64, 1), DataType.FLOAT32)
            inputFeature0.loadBuffer(studentTensorImage.buffer)
            val inputFeature1 =
                TensorBuffer.createFixedSize(intArrayOf(1, 64, 64, 1), DataType.FLOAT32)
            inputFeature1.loadBuffer(currentTensorImage.buffer)

            // Run inference
            val outputs = model.process(inputFeature0, inputFeature1)

            // Get the output
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            // Log the output
            val outputFloatArray = outputFeature0.floatArray
            outputFloatArray.forEach { value ->
                if (value > 0.5) {
                    Log.d(Constant.STUDENT_LOG, "Output : Yes")
                    mFaceVerifyState.postValue(true)
                } else {
                    Log.d(Constant.STUDENT_LOG, "Output : No")
                    mFaceVerifyState.postValue(false)
                }
            }

        } catch (e: Exception) {
            mFaceVerifyState.postValue(false)
            Log.d(Constant.STUDENT_LOG, "Face Model $e")
        }
    }


    private fun convertDocumentSnapshotToUser(searchUser: List<com.google.firebase.firestore.DocumentSnapshot>): ArrayList<SignIn?> {
        val list: ArrayList<SignIn?> = ArrayList()
        searchUser.forEach { data ->
            val home = data.toObject(SignIn::class.java)
            list.add(home)
        }
        return list
    }

    private fun convertDocumentSnapshotToJoinedClassroom(searchClassrooms: List<com.google.firebase.firestore.DocumentSnapshot>): ArrayList<JoinClassroom?> {
        val list: ArrayList<JoinClassroom?> = ArrayList()
        searchClassrooms.forEach { data ->
            val home = data.toObject(JoinClassroom::class.java)
            list.add(home)
        }
        return list
    }

    private fun convertDocumentSnapshotToClass(classes: List<com.google.firebase.firestore.DocumentSnapshot>): ArrayList<Class?> {
        val list: ArrayList<Class?> = ArrayList()
        classes.forEach { data ->
            val home = data.toObject(Class::class.java)
            list.add(home)
        }
        return list
    }

    private fun convertDocumentSnapshotToAttendance(attendances: List<com.google.firebase.firestore.DocumentSnapshot>): ArrayList<MarkedAttendance?> {
        val list: ArrayList<MarkedAttendance?> = ArrayList()
        attendances.forEach { data ->
            val home = data.toObject(MarkedAttendance::class.java)
            list.add(home)
        }
        return list
    }

    private fun convertDocumentSnapshotToFaces(faces: List<com.google.firebase.firestore.DocumentSnapshot>): HashMap<String, FaceClassifier.Recognition> {
        val hash: HashMap<String, FaceClassifier.Recognition> = HashMap()
        faces.forEach { face ->
            val faceData = face.toObject(Face::class.java)
            if (faceData != null) {
                Log.d(Constant.REPOSITORY_LOG, "Embedding String ${faceData.embedding}")
                val embedding =
                    EmBeddingConverter.getEmbeddingFromString(faceData.embedding, faceData.name)
                Log.d(Constant.REPOSITORY_LOG, "Embedding ${embedding.embeeding}")
                hash[faceData.name] = embedding
                Log.d(Constant.REPOSITORY_LOG, "Hash ${hash[faceData.name]}")
            }
        }
        return hash
    }

}
