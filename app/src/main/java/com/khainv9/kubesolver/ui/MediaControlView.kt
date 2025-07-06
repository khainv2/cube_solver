package com.khainv9.kubesolver.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import com.khainv9.kubesolver.R

/**
 * MediaControlView - Điều khiển media với các nút điều khiển đầy đủ
 */
class MediaControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private lateinit var btnFullscreen: ImageButton
    private lateinit var btnFirst: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnLast: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var progressBar: SeekBar
    private lateinit var stepInfo: TextView
    
    private var isPlaying = false
    private var currentStep = 0
    private var totalSteps = 0
    private var currentSpeed = AnimationSpeed.NORMAL
    
    enum class AnimationSpeed(val duration: Int) {
        SLOW(2000),
        NORMAL(1000),
        FAST(500),
        VERY_FAST(250)
    }
    
    interface OnControlListener {
        fun onPlay()
        fun onPause()
        fun onPrevious()
        fun onNext()
        fun onSeek(position: Int)
        fun onFirst()
        fun onLast()
        fun onFullscreen()
        fun onSpeedChanged(speed: AnimationSpeed)
        fun onResetView()
    }
    
    private var controlListener: OnControlListener? = null
    
    init {
        initView()
    }
    
    private fun initView() {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_media_control, this, true)
        
        btnFullscreen = findViewById(R.id.btn_fullscreen)
        btnFirst = findViewById(R.id.btn_first)
        btnPrevious = findViewById(R.id.btn_previous)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnNext = findViewById(R.id.btn_next)
        btnLast = findViewById(R.id.btn_last)
        btnSettings = findViewById(R.id.btn_settings)
        progressBar = findViewById(R.id.progress_bar)
        stepInfo = findViewById(R.id.step_info)
        
        setupListeners()
        updateUI()
    }
    
    private fun setupListeners() {
        btnFullscreen.setOnClickListener {
            controlListener?.onFullscreen()
        }
        
        btnFirst.setOnClickListener {
            controlListener?.onFirst()
        }
        
        btnPrevious.setOnClickListener {
            controlListener?.onPrevious()
        }
        
        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pause()
            } else {
                play()
            }
        }
        
        btnNext.setOnClickListener {
            controlListener?.onNext()
        }
        
        btnLast.setOnClickListener {
            controlListener?.onLast()
        }
        
        btnSettings.setOnClickListener { view ->
            showSettingsPopup(view)
        }
        
        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    controlListener?.onSeek(progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    fun play() {
        isPlaying = true
        updateUI()
        controlListener?.onPlay()
    }
    
    fun pause() {
        isPlaying = false
        updateUI()
        controlListener?.onPause()
    }
    
    fun setTotalSteps(total: Int) {
        totalSteps = total
        progressBar.max = total
        updateUI()
    }
    
    fun getTotalSteps(): Int = totalSteps
    
    fun setCurrentStep(step: Int) {
        currentStep = step
        progressBar.progress = step
        updateUI()
    }
    
    fun getCurrentStep(): Int = currentStep
    
    fun setOnControlListener(listener: OnControlListener?) {
        controlListener = listener
    }
    
    private fun updateUI() {
        btnPlayPause.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
        
        stepInfo.text = String.format("Bước %d / %d", currentStep + 1, totalSteps)
        
        btnFirst.isEnabled = currentStep > 0
        btnPrevious.isEnabled = currentStep > 0
        btnNext.isEnabled = currentStep < totalSteps - 1
        btnLast.isEnabled = currentStep < totalSteps - 1
    }
    
    private fun showSettingsPopup(anchor: View) {
        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.popup_settings, popup.menu)
        
        // Set current speed as checked
        val currentSpeedItem = when (currentSpeed) {
            AnimationSpeed.SLOW -> popup.menu.findItem(R.id.menu_speed_slow)
            AnimationSpeed.NORMAL -> popup.menu.findItem(R.id.menu_speed_normal)
            AnimationSpeed.FAST -> popup.menu.findItem(R.id.menu_speed_fast)
            AnimationSpeed.VERY_FAST -> popup.menu.findItem(R.id.menu_speed_very_fast)
        }
        currentSpeedItem?.isChecked = true
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_speed_slow -> {
                    setAnimationSpeed(AnimationSpeed.SLOW)
                    true
                }
                R.id.menu_speed_normal -> {
                    setAnimationSpeed(AnimationSpeed.NORMAL)
                    true
                }
                R.id.menu_speed_fast -> {
                    setAnimationSpeed(AnimationSpeed.FAST)
                    true
                }
                R.id.menu_speed_very_fast -> {
                    setAnimationSpeed(AnimationSpeed.VERY_FAST)
                    true
                }
                R.id.menu_reset_view -> {
                    controlListener?.onResetView()
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    fun setAnimationSpeed(speed: AnimationSpeed) {
        currentSpeed = speed
        controlListener?.onSpeedChanged(speed)
    }
    
    fun getCurrentSpeed(): AnimationSpeed = currentSpeed
}
