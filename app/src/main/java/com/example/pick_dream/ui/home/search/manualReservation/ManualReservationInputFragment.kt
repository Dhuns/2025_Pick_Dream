package com.example.pick_dream.ui.home.search.manualReservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pick_dream.R
import com.google.android.material.button.MaterialButton
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.activityViewModels
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog

class ManualReservationInputFragment : Fragment() {
    private val reservationViewModel: ManualReservationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manual_reservation_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnBack = view.findViewById<View>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        val btnReserve = view.findViewById<MaterialButton>(R.id.btnReserve)
        val etEventName = view.findViewById<EditText>(R.id.etEventName)
        val etEventDetail = view.findViewById<EditText>(R.id.etEventDetail)
        val etEventTarget = view.findViewById<EditText>(R.id.etEventTarget)
        val etEventPeople = view.findViewById<EditText>(R.id.etEventPeople)

        fun updateButtonState() {
            val isFilled = etEventName.text.isNotBlank() &&
                    etEventDetail.text.isNotBlank() &&
                    etEventTarget.text.isNotBlank() &&
                    etEventPeople.text.isNotBlank()
            btnReserve.isEnabled = isFilled
            if (isFilled) {
                btnReserve.setBackgroundColor(resources.getColor(R.color.primary_400, null))
                btnReserve.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                btnReserve.setBackgroundColor(resources.getColor(R.color.primary_050, null))
                btnReserve.setTextColor(resources.getColor(R.color.primary_400, null))
            }
        }

        fun addWatcher(editText: EditText) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateButtonState()
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        addWatcher(etEventName)
        addWatcher(etEventDetail)
        addWatcher(etEventTarget)
        addWatcher(etEventPeople)
        updateButtonState()

        btnReserve.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_reservation_complete, null)
            val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            val background = android.graphics.drawable.GradientDrawable()
            background.setColor(android.graphics.Color.WHITE)
            val radius = resources.displayMetrics.density * 16 // 16dp
            background.cornerRadius = radius
            dialog.setOnShowListener {
                dialog.window?.setBackgroundDrawable(background)
            }
            dialogView.findViewById<TextView>(R.id.btnDialogOk).setOnClickListener {
                dialog.dismiss()
                findNavController().navigate(R.id.homeFragment)
            }
            dialog.show()
        }
        val tvBuildingInfo = view.findViewById<TextView>(R.id.tvBuildingInfo)
        val tvRoomName = view.findViewById<TextView>(R.id.tvRoomName)
        tvBuildingInfo.text = arguments?.getString("building") ?: ""
        tvRoomName.text = arguments?.getString("roomName") ?: ""
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }
}
