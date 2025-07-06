package com.khainv9.kubesolver.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.khainv9.kubesolver.R

/**
 * StatusView - Hiển thị trạng thái hiện tại của app
 */
class StatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusIcon: ImageView
    
    enum class ScanStatus {
        IDLE,           // Chưa bắt đầu
        SCANNING,       // Đang quét
        FACE_COMPLETE,  // Hoàn thành 1 mặt
        ALL_COMPLETE,   // Hoàn thành tất cả
        SOLVING,        // Đang giải
        SOLVED,         // Đã giải xong
        ERROR           // Lỗi
    }
    
    init {
        initView()
    }
    
    private fun initView() {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.view_status, this, true)
        
        statusText = findViewById(R.id.status_text)
        progressBar = findViewById(R.id.progress_bar)
        statusIcon = findViewById(R.id.status_icon)
        
        // Set default status
        setStatus("Sẵn sàng", ScanStatus.IDLE)
    }
    
    fun setStatus(text: String, status: ScanStatus) {
        statusText.text = text
        
        when (status) {
            ScanStatus.IDLE -> {
                progressBar.visibility = View.GONE
                statusIcon.setImageResource(android.R.drawable.ic_media_play)
                statusIcon.visibility = View.VISIBLE
            }
            ScanStatus.SCANNING -> {
                progressBar.visibility = View.VISIBLE
                statusIcon.visibility = View.GONE
            }
            ScanStatus.FACE_COMPLETE -> {
                progressBar.visibility = View.GONE
                statusIcon.setImageResource(android.R.drawable.checkbox_on_background)
                statusIcon.visibility = View.VISIBLE
            }
            ScanStatus.ALL_COMPLETE -> {
                progressBar.visibility = View.GONE
                statusIcon.setImageResource(android.R.drawable.checkbox_on_background)
                statusIcon.visibility = View.VISIBLE
            }
            ScanStatus.SOLVING -> {
                progressBar.visibility = View.VISIBLE
                statusIcon.visibility = View.GONE
            }
            ScanStatus.SOLVED -> {
                progressBar.visibility = View.GONE
                statusIcon.setImageResource(android.R.drawable.checkbox_on_background)
                statusIcon.visibility = View.VISIBLE
            }
            ScanStatus.ERROR -> {
                progressBar.visibility = View.GONE
                statusIcon.setImageResource(android.R.drawable.ic_delete)
                statusIcon.visibility = View.VISIBLE
            }
        }
    }
    
    fun setProgress(progress: Int) {
        progressBar.progress = progress
    }
}
