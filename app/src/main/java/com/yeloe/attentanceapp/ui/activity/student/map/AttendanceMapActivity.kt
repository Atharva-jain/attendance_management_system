package com.yeloe.attentanceapp.ui.activity.student.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.yeloe.attentanceapp.MainActivity
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.ActivityAttendanceMapBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.student.MarkedAttendance
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.repo.AttendanceRepository
import com.yeloe.attentanceapp.ui.activity.face_detector.FaceDetectionActivity
import com.yeloe.attentanceapp.ui.activity.student.FaceVerificationActivity
import com.yeloe.attentanceapp.ui.activity.student.StudentActivity
import com.yeloe.attentanceapp.ui.face_classification.FaceClassifier
import com.yeloe.attentanceapp.ui.fragment.authentication.ScanFaceDialogFragment
import com.yeloe.attentanceapp.ui.fragment.student.classes.MarkedAttendanceDialogFragment
import com.yeloe.attentanceapp.ui.fragment.student.classes.OnMarkedAttendanceInterface
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetBitmapFromUrl
import com.yeloe.attentanceapp.utils.GetUid
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast
import com.yeloe.attentanceapp.utils.cameraPermissionRequest
import com.yeloe.attentanceapp.utils.isPermissionGranted
import com.yeloe.attentanceapp.utils.openPermissionSetting
import com.yeloe.attentanceapp.view_model.AttendanceViewModel
import com.yeloe.attentanceapp.view_model.factory.AttendanceViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AttendanceMapActivity : AppCompatActivity(), OnMapReadyCallback, OnMarkedAttendanceInterface {

    lateinit var binding: ActivityAttendanceMapBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var mCurrentLocation: LatLng = LatLng(0.0, 0.0)

    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mAttendanceCollection =
        mFirebaseFireStoreInstance.collection(Constant.attendance)


    private lateinit var mMarkedAttendanceDialog: MarkedAttendanceDialogFragment
    lateinit var mAttendanceViewModel: AttendanceViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.attendanceMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val repository = AttendanceRepository()
        val viewModelProviderFactory = AttendanceViewModelFactory(application, repository)
        mAttendanceViewModel = ViewModelProvider(
            this, viewModelProviderFactory
        )[AttendanceViewModel::class.java]

        binding.backNavigateImageView.setOnClickListener {
            NavUtils.navigateUpFromSameTask(this)
        }

        binding.refreshLocationCardView.setOnClickListener {
            try {
                setTeacherMarker()
            } catch (e: Exception) {
                ShowToast.showToast(
                    this,
                    "Unable to retrieve your location. Please try again later."
                )
            }
        }

        mAttendanceViewModel.mCheckAttendanceMarked.observe(this) { response ->
            when (response) {
                is Resources.Success -> {
                    val data = response.data
                    Log.d(Constant.STUDENT_LOG, "\n $data")
                    if (data != null) {
                        if (data) {
                            val uid = GetUid.getUid()
                            if (uid != null) {
                                val attendanceUid = mAttendanceCollection.document().id
                                val date = Calendar.getInstance().time
                                val markedAttendance = MarkedAttendance(
                                    uid = uid,
                                    attendanceUid = attendanceUid,
                                    teacherUid = mJoinedClassroom.teacherUid,
                                    joinClassRoomUid = mJoinedClassroom.joinClassRoomUid,
                                    classroomUid = mJoinedClassroom.classroomUid,
                                    classUid = mClass.classUid,
                                    classroomName = mJoinedClassroom.classroomName,
                                    classroomCollege = mJoinedClassroom.classroomCollege,
                                    classroomBranch = mJoinedClassroom.classroomBranch,
                                    classroomYear = mJoinedClassroom.classroomYear,
                                    classroomSemester = mJoinedClassroom.classroomSemester,
                                    classCode = mClass.classCode,
                                    teacherCollege = mJoinedClassroom.teacherCollege,
                                    teacherImage = mJoinedClassroom.teacherImage,
                                    timeStamp = date,
                                    teacherEmail = mJoinedClassroom.teacherEmail,
                                    teacherName = mJoinedClassroom.teacherName,
                                    studentYear = mJoinedClassroom.studentYear,
                                    studentSemester = mJoinedClassroom.studentSemester,
                                    studentProfileImage = mJoinedClassroom.studentProfileImage,
                                    studentName = mJoinedClassroom.studentName,
                                    studentFaceImage = mJoinedClassroom.studentFaceImage,
                                    studentCollege = mJoinedClassroom.studentCollege,
                                    studentBranch = mJoinedClassroom.studentBranch,
                                    keywords = mJoinedClassroom.keywords,
                                    location = mClass.location
                                )
                                mAttendanceViewModel.markedAttendance(
                                    markedAttendance
                                )
                            } else {
                                ShowToast.showToast(
                                    this,
                                    "Something went wrong, please try again later!"
                                )
                            }
                        } else {
                            ShowToast.showToast(
                                this,
                                "Your attendance is already marked..."
                            )
                            setMarkedAttendanceProgressBarVisibility(false)
                        }
                    } else {
                        ShowToast.showToast(this, "Unable to Marked Attendance")
                        setMarkedAttendanceProgressBarVisibility(false)
                    }
                }

                is Resources.Error -> {
                    response.message.let { message ->
                        Log.d(Constant.STUDENT_LOG, "Error $message")
                        ShowToast.showToast(this, message.toString())
                    }
                    setMarkedAttendanceProgressBarVisibility(false)
                }

                is Resources.Loading -> {
                    setMarkedAttendanceProgressBarVisibility(true)
                }

                else -> {
                    mAttendanceViewModel.mMarkedAttendanceState.postValue(
                        Resources.Completed()
                    )
                }
            }
        }

        mAttendanceViewModel.mMarkedAttendanceState.observe(this) { response ->
            when (response) {
                is Resources.Success -> {
                    val data = response.data
                    Log.d(Constant.STUDENT_LOG, "\n $data")
                    if (data != null) {
                        if (data) {
                            ShowToast.showToast(this, "Attendance Marked!!")
                            NavUtils.navigateUpFromSameTask(this)
                        } else {
                            ShowToast.showToast(
                                this,
                                "Unable to Marked Attendance"
                            )
                            setMarkedAttendanceProgressBarVisibility(false)
                        }
                    } else {
                        ShowToast.showToast(this, "Unable to Marked Attendance")
                        setMarkedAttendanceProgressBarVisibility(false)
                    }
                }

                is Resources.Error -> {
                    response.message.let { message ->
                        Log.d(Constant.STUDENT_LOG, "Error $message")
                        ShowToast.showToast(this, message.toString())
                    }
                    setMarkedAttendanceProgressBarVisibility(false)
                }

                is Resources.Loading -> {
                    setMarkedAttendanceProgressBarVisibility(true)
                }

                else -> {
                    mAttendanceViewModel.mMarkedAttendanceState.postValue(
                        Resources.Completed()
                    )
                }
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            mMap = googleMap
            setTeacherMarker()
        } catch (e: Exception) {
            Log.d(Constant.STUDENT_LOG, "onMapReady $e")
        }

    }

    private fun setMarkedAttendanceProgressBarVisibility(value: Boolean) {
        if (value) {
            binding.markedAttendanceLinearProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.markedAttendanceLinearProgressIndicator.visibility = View.GONE
        }
    }

    private fun setTeacherMarker() {
        try {
            Log.d(Constant.STUDENT_LOG, "Called setTeacherMarker ")
            val lat = mClass.location.lat.toDouble()
            val lng = mClass.location.lng.toDouble()
            //21.4593687,80.192919
            Log.d(Constant.STUDENT_LOG, "$lat $lng")
            //val teacherLocation = LatLng(21.4592075,80.1903645)
            val teacherLocation = LatLng(lat, lng)
            val teacherMarker = MarkerOptions().position(teacherLocation).title(mClass.teacherName)
            mMap.addMarker(teacherMarker)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(teacherLocation))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(teacherLocation))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(teacherLocation, 18f))

            val circleOptions =
                CircleOptions().center(teacherLocation).radius(Constant.CIRCLE_RADIUS)
                    .fillColor(Color.parseColor(Constant.transparent_light_blue_color))
                    .strokeColor(Color.parseColor(Constant.transparent_dark_blue_color))

            mMap.addCircle(
                circleOptions
            )

            setCurrentLocation(teacherMarker, circleOptions, teacherLocation)

        } catch (e: Exception) {
            Log.d(Constant.STUDENT_LOG, "Teacher Marker")
        }

    }

    private fun setCurrentLocation(
        teacherMarkerOptions: MarkerOptions,
        circle: CircleOptions,
        teacherLocation: LatLng
    ) {
        Log.d(Constant.STUDENT_LOG, "Called setCurrentLocation ")
        if (ActivityCompat.checkSelfPermission(
                (this), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                (this), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
            location?.let {

                // Location found, handle it
                val latitude = it.latitude
                val longitude = it.longitude

                Log.d(Constant.TEACHER_LOG, "Latitude: $latitude, Longitude: $longitude")

                mCurrentLocation = LatLng(latitude, longitude)

                checkLocationAreSame(
                    LatLng(latitude, longitude),
                    teacherMarkerOptions,
                    circle,
                    teacherLocation
                )

            } ?: run {

                ShowToast.showToast(
                    this, "Unable to retrieve your location. Please try again later."
                )
                Log.d(Constant.TEACHER_LOG, "Location is null")
            }
        }.addOnFailureListener { exception: Exception ->
            // Handle any exceptions that occur during location retrieval

            ShowToast.showToast(
                this, "Unable to retrieve your location. Please try again later."
            )
            Log.e(Constant.TEACHER_LOG, "Failed to get location: ${exception.message}")
        }
    }

    private fun checkLocationAreSame(
        location: LatLng, marker: MarkerOptions, circle: CircleOptions, teacherLocation: LatLng
    ) {
        Log.d(Constant.STUDENT_LOG, "Called checkLocationAreSame ")
        try {
            if (location == marker.position) {
                //update the position of the coincident marker by applying a small multipler to its coordinates
                val newLat: Double =
                    location.latitude + (Math.random() - 0.5) / 1500 // * (Math.random() * (max - min) + min);
                val newLng: Double =
                    location.longitude + (Math.random() - 0.5) / 1500 // * (Math.random() * (max - min) + min);
                val finalLatLng = LatLng(newLat, newLng)

                val currentMarker = MarkerOptions().position(finalLatLng).title("Your Location")

                mMap.addMarker(currentMarker)

                checkLocationWithRadius(location, circle, teacherLocation)

            } else {
                val currentMarker =
                    MarkerOptions().position(LatLng(location.latitude, location.longitude))
                        .title("Your Location")

                mMap.addMarker(currentMarker)

                checkLocationWithRadius(location, circle, teacherLocation)
            }
        } catch (e: Exception) {
            val currentMarker =
                MarkerOptions().position(LatLng(location.latitude, location.longitude))
                    .title("Your Location")

            mMap.addMarker(currentMarker)

            checkLocationWithRadius(location, circle, teacherLocation)
        }

    }

    private fun checkLocationWithRadius(
        currentLocation: LatLng,
        circle: CircleOptions,
        teacherLocation: LatLng
    ) {
        val distance = FloatArray(1)

        Log.d(Constant.STUDENT_LOG, "Called checkLocationWithRadius ")

        val location = Location.distanceBetween(
            currentLocation.latitude,
            currentLocation.longitude,
            teacherLocation.latitude,
            teacherLocation.longitude,
            distance
        )

        if (distance[0] / Constant.CIRCLE_RADIUS > 1) {
            ShowToast.showToast(this, "You are not in Location")
        } else {
            //current location is within circle
            ShowToast.showToast(this, "You are with in Location")

            mMarkedAttendanceDialog = MarkedAttendanceDialogFragment(
                classes = mClass, joinClassroom = mJoinedClassroom, this
            )
            mMarkedAttendanceDialog.show(supportFragmentManager, "MarkedAttendance")
        }
    }


    companion object {
        private val TAG = AttendanceMapActivity::class.simpleName
        private var mClass = Class()
        private var mJoinedClassroom = JoinClassroom()
        fun startActivity(context: Context, classes: Class, joinClassroom: JoinClassroom) {
            Intent(context, AttendanceMapActivity::class.java).also {
                this.mClass = classes
                this.mJoinedClassroom = joinClassroom
                context.startActivity(it)
            }
        }
    }

    override fun onMarkedAttendance() {
        val uid = GetUid.getUid()
        if (uid != null) {
            mAttendanceViewModel.checkAttendanceMarked(mClass.classUid, uid)
        } else {
            ShowToast.showToast(this, "Something went wrong please try again later")
        }

    }

    override fun onStartCamera(faces: HashMap<String, FaceClassifier.Recognition>) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                // Perform network operation in the background thread
                val faceImage = GetBitmapFromUrl.getBitmapFromURL(mJoinedClassroom.studentFaceImage)

                withContext(Dispatchers.Main) {
                    // Switch back to the main thread to update UI
                    if (faceImage != null) {
                        FaceVerificationActivity.startActivity(
                            this@AttendanceMapActivity,
                            mClass,
                            mJoinedClassroom,
                            faceImage,
                            faces
                        ) {
                            try {
                                mMarkedAttendanceDialog.setFaceVerifiedState(it)
                            } catch (e: java.lang.Exception) {
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            Log.d(Constant.STUDENT_LOG, "Error on face MarkedAttendanceDialogFragment $e")
        }
    }

}

//     val attendanceUid = mAttendanceCollection.document().id
//            val date = Calendar.getInstance().time
//            val markedAttendance = MarkedAttendance(
//                uid = uid,
//                attendanceUid = attendanceUid,
//                teacherUid = mJoinedClassroom.teacherUid,
//                joinClassRoomUid = mJoinedClassroom.joinClassRoomUid,
//                classroomUid = mJoinedClassroom.classroomUid,
//                classUid = mClass.classUid,
//                classroomName = mJoinedClassroom.classroomName,
//                classroomCollege = mJoinedClassroom.classroomCollege,
//                classroomBranch = mJoinedClassroom.classroomBranch,
//                classroomYear = mJoinedClassroom.classroomYear,
//                classroomSemester = mJoinedClassroom.classroomSemester,
//                classCode = mClass.classCode,
//                teacherCollege = mJoinedClassroom.teacherCollege,
//                teacherImage = mJoinedClassroom.teacherImage,
//                timeStamp = date,
//                teacherEmail = mJoinedClassroom.teacherEmail,
//                teacherName = mJoinedClassroom.teacherName,
//                studentYear = mJoinedClassroom.studentYear,
//                studentSemester = mJoinedClassroom.studentSemester,
//                studentProfileImage = mJoinedClassroom.studentProfileImage,
//                studentName = mJoinedClassroom.studentName,
//                studentFaceImage = mJoinedClassroom.studentFaceImage,
//                studentCollege = mJoinedClassroom.studentCollege,
//                studentBranch = mJoinedClassroom.studentBranch,
//                keywords = mJoinedClassroom.keywords,
//                location = mClass.location
//            )
//
//            mAttendanceViewModel.markedAttendance(
//                markedAttendance
//            )


