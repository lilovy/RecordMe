package com.lilovy.recordme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.lilovy.recordme.api.Transcribe
import com.lilovy.recordme.databinding.ActivityMainBinding
import com.lilovy.recordme.db.AppDatabase
import com.lilovy.recordme.db.AudioRecord
import com.lilovy.recordme.tools.Timer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : AppCompatActivity(), BottomSheet.OnClickListener, Timer.OnTimerUpdateListener {

    private lateinit var fileName: String
    private lateinit var dirPath: String
    private var recorder: MediaRecorder? = null
    private var recording = false
    private var onPause = false
    private var refreshRate : Long = 60
    private lateinit var timer: Timer
    private lateinit var handler: Handler

    private lateinit var content: String

    private lateinit var binding: ActivityMainBinding

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        handler = Handler(Looper.myLooper()!!)

        binding.recordBtn.setOnClickListener {
            when {
                onPause -> resumeRecording()
                recording -> pauseRecording()
                else -> startRecording()
            }
        }

        binding.doneBtn.setOnClickListener {
            stopRecording()
            showBottomSheet()
        }

        binding.listBtn.setOnClickListener {
            startActivity(Intent(this, ListingActivity::class.java))
        }

        binding.deleteBtn.setOnClickListener {
            stopRecording()

            File(dirPath+fileName).delete()
        }
        binding.deleteBtn.isClickable = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    private fun startRecording(){

        if(!permissionToRecordAccepted){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
            return
        }

        binding.listBtn.visibility = View.GONE
        binding.doneBtn.visibility = View.VISIBLE
        binding.deleteBtn.isClickable = true
        binding.deleteBtn.setImageResource(R.drawable.ic_delete_enabled)

        recording = true
        timer = Timer(this)
        timer.start()

        val pattern = "yyyy.MM.dd_hh.mm.ss"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(Date())

        dirPath = "${externalCacheDir?.absolutePath}/"
        fileName = "voice_record_${date}.mp3"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)

            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            setOutputFile(dirPath+fileName)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG,"prepare() failed")
            }

            start()
        }

        binding.recordBtn.setImageResource(R.drawable.ic_pause)

        animatePlayerView()

    }

    private fun animatePlayerView(){
        if (recording && !onPause){
            var amp = recorder!!.maxAmplitude

            handler.postDelayed(
                Runnable {
                    kotlin.run { animatePlayerView() }
                }, refreshRate
            )
        }
    }

    private fun pauseRecording(){
        onPause = true
        recorder?.apply {
            pause()
        }
        binding.recordBtn.setImageResource(R.drawable.ic_record)
        timer.pause()

    }

    private fun resumeRecording(){
        onPause = false
        recorder?.apply {
            resume()
        }
        binding.recordBtn.setImageResource(R.drawable.ic_pause)
        animatePlayerView()
        timer.start()
    }

    private fun stopRecording(){
        recording = false
        onPause = false
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        binding.recordBtn.setImageResource(R.drawable.ic_record)

        binding.listBtn.visibility = View.VISIBLE
        binding.doneBtn.visibility = View.GONE
        binding.deleteBtn.isClickable = false
        binding.deleteBtn.setImageResource(R.drawable.ic_delete_disabled)

//        binding.playerView.reset()
        try {
            timer.stop()
        }catch (_: Exception){}

        binding.timerView.text = "00:00.00"
    }

    private fun showBottomSheet(){
        var bottomSheet = BottomSheet(dirPath, fileName, this)
        bottomSheet.show(supportFragmentManager, LOG_TAG)

    }

    override fun onCancelClicked() {
        Toast.makeText(this, "Audio record deleted", Toast.LENGTH_SHORT).show()
        stopRecording()
    }

    override fun onOkClicked(filePath: String, filename: String) {

        binding.progressBar.visibility = ProgressBar.VISIBLE

        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords").build()

        val duration = timer.format().split(".")[0]
        stopRecording()

        GlobalScope.launch {
            content = withContext(Dispatchers.IO) {
                Transcribe().getTranscribe(filePath).toString()
            }

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = ProgressBar.GONE
            }

            db.audioRecordDAO().insert(AudioRecord(filename, filePath, Date().time, duration, content))
        }

    }

    override fun onTimerUpdate(duration: String) {
        runOnUiThread{
            if(recording)
                binding.timerView.text = duration
        }
    }

}