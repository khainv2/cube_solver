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

        // Set default status
//        setStatus("Sẵn sàng", ScanStatus.IDLE)
    }
    
    fun setStatus(text: String, status: ScanStatus) {
        statusText.text = text
        
        when (status) {
            ScanStatus.IDLE -> {
                progressBar.visibility = View.GONE
            }
            ScanStatus.SCANNING -> {
                progressBar.visibility = View.VISIBLE
            }
            ScanStatus.FACE_COMPLETE -> {
                progressBar.visibility = View.GONE
            }
            ScanStatus.ALL_COMPLETE -> {
                progressBar.visibility = View.GONE
            }
            ScanStatus.SOLVING -> {
                progressBar.visibility = View.VISIBLE
            }
            ScanStatus.SOLVED -> {
                progressBar.visibility = View.GONE
            }
            ScanStatus.ERROR -> {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    fun setProgress(progress: Int) {
        progressBar.progress = progress
    }
}
