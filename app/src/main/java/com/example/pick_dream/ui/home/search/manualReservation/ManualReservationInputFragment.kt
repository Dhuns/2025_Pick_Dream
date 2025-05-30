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
import com.example.pick_dream.model.Reservation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.navigation.NavOptions

class ManualReservationInputFragment : Fragment() {
    private val reservationViewModel: ManualReservationViewModel by activityViewModels()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manual_reservation_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnBack = view.findViewById<View>(R.id.btnBack)
        val btnReserve = view.findViewById<MaterialButton>(R.id.btnReserve)
        val etEventName = view.findViewById<EditText>(R.id.etEventName)
        val etEventDetail = view.findViewById<EditText>(R.id.etEventDetail)
        val etEventTarget = view.findViewById<EditText>(R.id.etEventTarget)
        val etEventPeople = view.findViewById<EditText>(R.id.etEventPeople)
        val tvBuildingInfo = view.findViewById<TextView>(R.id.tvBuildingInfo)
        val tvRoomName = view.findViewById<TextView>(R.id.tvRoomName)

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

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnReserve.setOnClickListener {
            val eventDescription = etEventDetail.text.toString()
            val eventName = etEventName.text.toString()
            val eventParticipants = etEventPeople.text.toString().toIntOrNull() ?: 0
            val eventTarget = etEventTarget.text.toString()

            if (eventDescription.isBlank() || eventName.isBlank() || eventParticipants <= 0 || eventTarget.isBlank()) {
                Toast.makeText(context, "행사명, 목적, 인원수, 참여대상을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. User 컬렉션에서 학번(userID) 조회
            db.collection("User").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val studentId = document.getString("studentId") ?: ""
                    if (studentId.isBlank()) {
                        Toast.makeText(context, "학번 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    // 2. 예약 데이터 생성
                    arguments?.let { args ->
                        val roomId = args.getString("roomId") ?: ""
                        val reservation = Reservation(
                            userID = studentId,
                            roomID = roomId,
                            eventName = eventName,
                            eventDescription = eventDescription,
                            eventTarget = eventTarget,
                            eventParticipants = eventParticipants,
                            startTime = String.format("%d년 %d월 %d일 오후 %d시 %d분 0초 UTC+9",
                                args.getInt("selectedYear"),
                                args.getInt("selectedMonth") + 1,
                                args.getInt("selectedDay"),
                                args.getInt("startHour"),
                                args.getInt("startMinute")),
                            endTime = String.format("%d년 %d월 %d일 오후 %d시 %d분 0초 UTC+9",
                                args.getInt("selectedYear"),
                                args.getInt("selectedMonth") + 1,
                                args.getInt("selectedDay"),
                                args.getInt("endHour"),
                                args.getInt("endMinute")),
                            status = "대기"
                        )
                        // 3. Firestore에 저장
                        db.collection("Reservations")
                            .add(reservation)
                            .addOnSuccessListener {
                                val dialogView = layoutInflater.inflate(R.layout.dialog_reservation_complete, null)
                                val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomDialog)
                                    .setView(dialogView)
                                    .setCancelable(false)
                                    .create()
                                // 둥근 흰색 배경 적용
                                val background = android.graphics.drawable.GradientDrawable()
                                background.setColor(android.graphics.Color.WHITE)
                                val radius = resources.displayMetrics.density * 16 // 16dp
                                background.cornerRadius = radius
                                dialog.setOnShowListener {
                                    dialog.window?.setBackgroundDrawable(background)
                                }
                                dialogView.findViewById<android.widget.TextView>(R.id.btnDialogOk).setOnClickListener {
                                    dialog.dismiss()
                                    findNavController().navigate(
                                        R.id.homeFragment,
                                        null,
                                        NavOptions.Builder()
                                            .setPopUpTo(R.id.homeFragment, true)
                                            .build()
                                    )
                                }
                                dialog.show()
                            }
                            .addOnFailureListener {
                                android.widget.Toast.makeText(context, "대여 중 오류가 발생했습니다.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
        }

        // 이전 화면에서 전달받은 데이터 표시
        arguments?.let { args ->
            val building = args.getString("building") ?: ""
            val roomName = args.getString("roomName") ?: ""
            tvBuildingInfo.text = building
            tvRoomName.text = roomName
        }
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