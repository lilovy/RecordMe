package com.lilovy.recordme

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import com.lilovy.recordme.api.Transcribe
import com.lilovy.recordme.databinding.ActivityPlayerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerActivity : AppCompatActivity() {

    private val delay = 100L
    private lateinit var runnable : Runnable
    private lateinit var handler : Handler
    private lateinit var mediaPlayer : MediaPlayer

    private lateinit var content: String

    private lateinit var binding: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        val tvFilename = binding.tvFilename
        val seekBar = binding.seekBar
        val btnPlay = binding.btnPlay
        val btnTranscribe = binding.btnTranscibe
        val transcrb_txt = binding.transcrbTxt

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val filePath = intent.getStringExtra("filepath")
        val filename = intent.getStringExtra("filename")
        val transcript = intent.getStringExtra("transcript")

        tvFilename.text = filename
        transcrb_txt.text = transcript

        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }
        seekBar.max = mediaPlayer.duration

        handler = Handler(Looper.getMainLooper())
        playPausePlayer()

        mediaPlayer.setOnCompletionListener {
            stopPlayer()
        }

        btnPlay.setOnClickListener {
            playPausePlayer()
        }

//        btnTranscribe.setOnClickListener {
//            binding.progressBar.visibility = ProgressBar.VISIBLE
//
//            GlobalScope.launch {
//                content = withContext(Dispatchers.IO) {
//                        Transcribe().getTranscribe(filePath).toString()
//                }
//                withContext(Dispatchers.Main) {
//                    binding.progressBar.visibility = ProgressBar.GONE
//                    transcrb_txt.text = content
//                }
//            }
//
//        }


        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2) mediaPlayer.seekTo(p1)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

    }

     private fun playPausePlayer(){
        val btnPlay = binding.btnPlay
        val seekBar = binding.seekBar

        if(!mediaPlayer.isPlaying){
            mediaPlayer.start()
            btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_circle, theme)

            runnable = Runnable {
                val progress = mediaPlayer.currentPosition
                Log.d("progress", progress.toString())
                seekBar.progress = progress

                handler.postDelayed(runnable, delay)
            }
            handler.postDelayed(runnable, delay)
        }else{
            mediaPlayer.pause()
            btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)

            handler.removeCallbacks(runnable)
        }
    }

    private fun stopPlayer(){
        binding.btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
        handler.removeCallbacks(runnable)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.stop()
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }
}