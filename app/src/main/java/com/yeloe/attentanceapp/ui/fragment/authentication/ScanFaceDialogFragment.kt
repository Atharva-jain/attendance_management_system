package com.yeloe.attentanceapp.ui.fragment.authentication

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentScanFaceDialogBinding
import com.yeloe.attentanceapp.databinding.FragmentSignUpBasicDetailsFromBinding


class ScanFaceDialogFragment(
    private val faceImage: Bitmap,
    private val tryAgainListener: TryAgainToSelectFaceListener,
    private val selectedFaceImageListener: SelectedFaceOnClickListener
) : BottomSheetDialogFragment() {

    private var _binding: FragmentScanFaceDialogBinding? = null
    private val binding get() = _binding!!

    fun setImageOfFace(image: Bitmap?) {
        if (image != null) {
            binding.faceImageView.setImageBitmap(image)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanFaceDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (faceImage != null) {
            binding.faceImageView.setImageBitmap(faceImage)
        }

        binding.selectedButton.setOnClickListener {
            selectedFaceImageListener.selectedFaceOnClickListener(faceImage)
        }

        binding.tryAgainButton.setOnClickListener {
            tryAgainListener.tryAgainToSelectFaceListener()
        }

    }

}

interface SelectedFaceOnClickListener {
    fun selectedFaceOnClickListener(faceImage: Bitmap)
}

interface TryAgainToSelectFaceListener {
    fun tryAgainToSelectFaceListener()
}