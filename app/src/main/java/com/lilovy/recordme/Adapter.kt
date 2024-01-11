package com.lilovy.recordme

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lilovy.recordme.db.AudioRecord
import com.lilovy.recordme.databinding.ItemviewLayoutBinding
import java.text.SimpleDateFormat
import java.util.Date


class Adapter(
    private var audioRecords: List<AudioRecord>,
    private val listener: OnItemClickListener) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private var editMode = false
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemLongClick(position: Int)
    }

    inner class ViewHolder(binding: ItemviewLayoutBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        var filename: TextView = binding.filename
        var fileMeta: TextView = binding.fileMeta
        var checkBox: CheckBox = binding.checkbox

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }
        override fun onClick(p0: View?) {
            val position = adapterPosition // property of the recyclerview class
            if(position != RecyclerView.NO_POSITION)
                listener.onItemClick(position)
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition // property of the recyclerview class
            if(position != RecyclerView.NO_POSITION)
                listener.onItemLongClick(position)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemviewLayoutBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return audioRecords.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position != RecyclerView.NO_POSITION){
            var audioRecord = audioRecords[position]
            holder.filename.text = audioRecord.filename
            val sdf = SimpleDateFormat("dd/MM/yy")
            val netDate = Date(audioRecord.date)
            val date =sdf.format(netDate)

            holder.fileMeta.text = "${audioRecord.duration}  $date"

            Log.d("ListingTag", audioRecord.isChecked.toString())

            if(editMode) {
                holder.checkBox.visibility = View.VISIBLE
                if (audioRecord.isChecked)
                    holder.checkBox.isChecked = audioRecord.isChecked
            }else {
                holder.checkBox.visibility = View.GONE
                audioRecord.isChecked = false
                holder.checkBox.isChecked = false
            }
        }
    }

    fun setData(audioRecords: List<AudioRecord>){
        this.audioRecords = audioRecords
        notifyDataSetChanged()
    }

    fun setEditMode(mode: Boolean){
        editMode = mode
        notifyDataSetChanged()
    }

    fun isEditMode():Boolean{
        return editMode
    }

}
