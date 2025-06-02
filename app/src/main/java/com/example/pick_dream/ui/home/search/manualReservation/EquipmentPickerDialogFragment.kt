package com.example.pick_dream.ui.home.search.manualReservation

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R

class EquipmentPickerDialogFragment(
    private val onEquipmentsSelected: (selected: List<String>) -> Unit
) : DialogFragment() {
    private val equipmentList = listOf("빔프로젝터", "마이크", "노트북", "HDMI 케이블")
    private val checked = BooleanArray(equipmentList.size)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_equipment_picker, null)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerEquipment)
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        recycler.layoutManager = LinearLayoutManager(context)
        val adapter = EquipmentAdapter(equipmentList, checked)
        recycler.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        btnOk.setOnClickListener {
            val selected = equipmentList.filterIndexed { idx, _ -> checked[idx] }
            onEquipmentsSelected(selected)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        return dialog
    }

    class EquipmentAdapter(private val items: List<String>, private val checked: BooleanArray) : RecyclerView.Adapter<EquipmentAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_multiple_choice, parent, false)
            return ViewHolder(v)
        }
        override fun getItemCount() = items.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val checkedTextView = holder.itemView.findViewById<android.widget.CheckedTextView>(android.R.id.text1)
            checkedTextView.text = items[position]
            checkedTextView.isChecked = checked[position]
            checkedTextView.setOnClickListener {
                checked[position] = !checked[position]
                checkedTextView.isChecked = checked[position]
            }
        }
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
} 