package com.lilovy.recordme


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lilovy.recordme.databinding.ActivityListingBinding
import com.lilovy.recordme.db.AppDatabase
import com.lilovy.recordme.db.AudioRecord
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ListingActivity : AppCompatActivity(), Adapter.OnItemClickListener {
    private lateinit var binding: ActivityListingBinding

    private lateinit var adapter : Adapter
    private lateinit var audioRecords : List<AudioRecord>
    private lateinit var db : AppDatabase
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var allSelected = false
    private var nbSelected = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        val bottomSheet = binding.bottomSheet
        val recyclerview = binding.recyclerview
        val btnClose = binding.btnClose

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        audioRecords = emptyList()
        adapter = Adapter(audioRecords, this)

        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords")
            //.fallbackToDestructiveMigration()
            .build()

        fetchAll()

        btnClose.setOnClickListener {
            closeEditor()
        }

        binding.btnDelete.setOnClickListener {
            closeEditor()
            val toDelete : List<AudioRecord> = audioRecords.filter { it.isChecked }
            audioRecords = audioRecords.filter { !it.isChecked }
            GlobalScope.launch {
                db.audioRecordDAO().delete(toDelete)
                if(audioRecords.isEmpty())
                    fetchAll()
                else
                    adapter.setData(audioRecords)
            }
        }
    }


    private fun closeEditor(){
        allSelected = false
        adapter.setEditMode(false)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.editorBar.visibility = View.GONE
        nbSelected = 0
    }

    private fun fetchAll(){
        GlobalScope.launch {
            audioRecords = db.audioRecordDAO().getAll()
            adapter.setData(audioRecords)
        }
    }

    private fun updateBottomSheet(){
        val tvDelete = binding.tvDelete
        val btnDelete = binding.btnDelete

        when(nbSelected){
            0 -> {
                btnDelete.isClickable = false
                btnDelete.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_delete_disabled2, theme)
                tvDelete.setTextColor(resources.getColor(R.color.colorDisabled, theme))

            }
            1 -> {
                btnDelete.isClickable = true
                btnDelete.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, theme)
                tvDelete.setTextColor(resources.getColor(R.color.colorText, theme))

            }
            else -> {
                tvDelete.setTextColor(resources.getColor(R.color.colorText, theme))

            }
        }
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, PlayerActivity::class.java)
        val audioRecord = audioRecords[position]

        if(adapter.isEditMode()){
            Log.d("ITEMCHANGE", audioRecord.isChecked.toString())
            audioRecord.isChecked = !audioRecord.isChecked
            adapter.notifyItemChanged(position)

            nbSelected = if (audioRecord.isChecked) nbSelected+1 else nbSelected-1
            updateBottomSheet()

        }else{
            intent.putExtra("filepath", audioRecord.filePath)
            intent.putExtra("filename", audioRecord.filename)
            intent.putExtra("transcript", audioRecord.transcript)
            startActivity(intent)
        }

    }

    override fun onItemLongClick(position: Int) {
        adapter.setEditMode(true)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val audioRecord = audioRecords[position]

        audioRecord.isChecked = !audioRecord.isChecked

        nbSelected = if (audioRecord.isChecked) nbSelected+1 else nbSelected-1
        updateBottomSheet()

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        binding.editorBar.visibility = View.VISIBLE


    }

}