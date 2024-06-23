package com.yeloe.attentanceapp.ui.fragment.teacher.classes

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.yeloe.attentanceapp.databinding.FragmentCreateClassBinding
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.model.teacher.Location
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.utils.CheckGPSIsEnable
import com.yeloe.attentanceapp.utils.CheckInternetConnection

import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast
import java.util.Calendar
import java.util.Locale


class CreateClassFragment : Fragment() {


    private var _binding: FragmentCreateClassBinding? = null

    private val binding get() = _binding!!

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var mLocationUpdateState = false
    private var mLat = ""
    private var mLng = ""
    private var mAddress = ""

    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mClassesCollection =
        mFirebaseFireStoreInstance.collection(Constant.classesCollection)

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
    }

    private fun setVisibilityOfCreateClassProgressBar(value: Boolean) {
        if (value) {
            binding.createClassLinearProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.createClassLinearProgressIndicator.visibility = View.GONE
        }
    }

    private fun setUIOfLocationLayout(getLocation: Boolean) {
        if (getLocation) {
            binding.latLngLayout.visibility = View.VISIBLE
            binding.locationNameTextView.text = "Location: $mAddress"
            binding.latTextView.text = "Latitude: $mLat"
            binding.lngTextView.text = "Longitude: $mLng"
        } else {
            binding.latLngLayout.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateClassBinding.inflate(inflater, container, false)
        (activity as TeacherActivity).changeAppBarAccordingNavigation(Constant.TEACHER_CREATE_CLASS)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as TeacherActivity).binding.closeTopAppBar.setOnClickListener {
            try {
                findNavController().popBackStack()
            } catch (e: Exception) {
            }
        }

        binding.addCurrentLocationCardView.setOnClickListener {
            if (CheckGPSIsEnable.isGPSEnable((activity as TeacherActivity))) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request location permissions if not granted
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                } else {
                    // Location permissions are granted, proceed to get location
                    getCurrentLocation()
                }
            } else {
                ShowToast.showToast(
                    requireContext(),
                    "Please activate location services on your device."
                )
            }
        }

        (activity as TeacherActivity).mAttendanceViewModel.mCreateClassState.observe(
            viewLifecycleOwner
        ) { response ->

            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {

                            val data = response.data
                            Log.d(Constant.TEACHER_LOG, "\n $data")
                            if (data != null) {
                                if (data) {
                                    setVisibilityOfCreateClassProgressBar(false)
                                    ShowToast.showToast(
                                        requireContext(),
                                        "Class created successfully."
                                    )
                                    (activity as TeacherActivity).mAttendanceViewModel.mCreateClassState.postValue(
                                        Resources.Completed()
                                    )
                                    findNavController().popBackStack()
                                } else {
                                    ShowToast.showToast(
                                        requireContext(),
                                        "Unable to create class"
                                    )
                                    setVisibilityOfCreateClassProgressBar(false)
                                }
                            } else {
                                ShowToast.showToast(requireContext(), "Unable to create class")
                                setVisibilityOfCreateClassProgressBar(false)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.TEACHER_LOG, "Error $message")
                                ShowToast.showToast(requireContext(), message.toString())
                            }
                            setVisibilityOfCreateClassProgressBar(false)
                        }

                        is Resources.Loading -> {
                            setVisibilityOfCreateClassProgressBar(true)
                        }

                        else -> {
                            (activity as TeacherActivity).mAttendanceViewModel.mCreateClassState.postValue(
                                Resources.Completed()
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $e")
                ShowToast.showToast(requireContext(), e.message.toString())
            }


        }

        (activity as TeacherActivity).binding.taskButtonTopAppBar.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                if (mLocationUpdateState) {
                    val classCode = binding.classCodeTextInputEditText.text.toString()
                    Log.d(Constant.TEACHER_LOG, "Class code $classCode")
                    val date = Calendar.getInstance().time
                    if (validateClassCode(classCode = classCode)) {
                        val uid = FirebaseAuth.getInstance().uid
                        if (uid != null) {
                            val classUid = mClassesCollection.document().id
                            val classroom =
                                (activity as TeacherActivity).mAttendanceViewModel.getCreateClassroomData
                            val location = Location(
                                address = mAddress,
                                lat = mLat,
                                lng = mLng
                            )
                            val classData = Class(
                                classUid = classUid,
                                uid = uid,
                                classroomUid = classroom.classroomUid,
                                classCode = classCode,
                                location = location,
                                classroomSemester = classroom.classroomSemester,
                                classroomYear = classroom.classroomYear,
                                classroomBranch = classroom.classroomBranch,
                                classroomCollege = classroom.classroomCollege,
                                classroomName = classroom.classroomName,
                                teacherName = classroom.teacherName,
                                teacherEmail = classroom.teacherEmail,
                                teacherImage = classroom.teacherImage,
                                teacherCollege = classroom.teacherCollege,
                                timeStamp = date
                            )
                            (activity as TeacherActivity).mAttendanceViewModel.createClass(classes = classData)
                            (activity as TeacherActivity).mAttendanceViewModel.mCreateClassState.postValue(
                                Resources.Loading()
                            )
                        } else {
                            ShowToast.showToast(requireContext(), "Something went wrong")
                        }
                    }
                } else {
                    ShowToast.showToast(
                        requireContext(),
                        "Unable to retrieve your location. Please try again later."
                    )
                }
            } else {
                ShowToast.showToast(requireContext(), Constant.CHECK_INTERNET_CONNECTION)
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                (activity as TeacherActivity),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                (activity as TeacherActivity),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        setVisibilityOfCreateClassProgressBar(true)
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                location?.let {
                    mLocationUpdateState = true
                    // Location found, handle it
                    val latitude = it.latitude
                    val longitude = it.longitude
                    Log.d(Constant.TEACHER_LOG, "Latitude: $latitude, Longitude: $longitude")
                    setVisibilityOfCreateClassProgressBar(false)
                    // Use Geocoder to get location name
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val locationName = addresses?.firstOrNull()?.getAddressLine(0)
                    Log.d(Constant.TEACHER_LOG, "Location Name: $locationName")
                    mLat = "$latitude"
                    mLng = "$longitude"
                    mAddress = "$locationName"
                    setUIOfLocationLayout(true)
                    // Use locationName as per your requirement
                } ?: run {
                    // Location is null, handle the case
                    setVisibilityOfCreateClassProgressBar(false)
                    ShowToast.showToast(
                        requireContext(),
                        "Unable to retrieve your location. Please try again later."
                    )
                    Log.d(Constant.TEACHER_LOG, "Location is null")
                }
            }
            .addOnFailureListener { exception: Exception ->
                // Handle any exceptions that occur during location retrieval
                setVisibilityOfCreateClassProgressBar(false)
                ShowToast.showToast(
                    requireContext(), "Unable to retrieve your location. Please try again later."
                )
                Log.e(Constant.TEACHER_LOG, "Failed to get location: ${exception.message}")
            }
    }

    // Function to validate semester field
    private fun validateClassCode(classCode: String): Boolean {
        if (classCode.isEmpty()) {
            binding.classCodeTextInputEditText.error = "Semester cannot be empty"
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}