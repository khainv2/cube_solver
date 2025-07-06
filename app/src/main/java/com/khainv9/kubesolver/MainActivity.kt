package com.khainv9.kubesolver

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.khainv9.kubesolver.camera.EnhancedCameraView
import com.khainv9.kubesolver.cubeview.CubeColor
import com.khainv9.kubesolver.cubeview.RubiksCubeGLSurfaceView
import com.khainv9.kubesolver.ui.MediaControlView
import com.khainv9.kubesolver.ui.StatusView
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

/**
 * MainActivity - Activity chính của KubeSolver
 * Tích hợp camera scanning, 3D cube display, và media controls
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    // Views
    private lateinit var cameraView: EnhancedCameraView
    private lateinit var cubeView: RubiksCubeGLSurfaceView
    private lateinit var mediaControl: MediaControlView
    private lateinit var statusView: StatusView
    
    // Action buttons
    private lateinit var btnReset: Button
    private lateinit var btnSolve: Button
    
    // State
    private var currentState = AppState.SCAN_READY
    
    // OpenCV callback
    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    cameraView.enableView()
                    setupCameraListener()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }
    
    enum class AppState {
        SCAN_READY,      // Sẵn sàng quét
        SCANNING,        // Đang quét cube
        SCAN_COMPLETE,   // Quét xong, sẵn sàng giải
        SOLVING,         // Đang tính toán solution
        SOLUTION_READY,  // Có solution, sẵn sàng animate
        ANIMATING,       // Đang chạy animation
        COMPLETED        // Hoàn thành
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupEventListeners()
        updateUI()
    }
    
    private fun setupCameraListener() {
        // Setup camera face scanned listener
        cameraView.setOnFaceScannedListener(object : EnhancedCameraView.OnFaceScannedListener {
            override fun onFaceScanned(colors: Array<CubeColor>) {
                runOnUiThread {
                    handleFaceScanned(colors)
                }
            }
        })
    }
    
    private fun handleFaceScanned(colors: Array<CubeColor>) {
        Log.d(TAG, "Face scanned with ${colors.size} colors")
        
        // Update UI
        Toast.makeText(this, "Đã quét xong một mặt!", Toast.LENGTH_SHORT).show()
        
        // TODO: Implement face scanning logic
        // For now, just show that scanning is working
        currentState = AppState.SCAN_COMPLETE
        updateUI()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Resume cube view
        cubeView.onResume()
        
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }
    
    override fun onPause() {
        super.onPause()
        
        // Pause cube view
        cubeView.onPause()
        cameraView.disableView()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }
    
    private fun initViews() {
        // Initialize views
        statusView = findViewById(R.id.status_view)
        cameraView = findViewById(R.id.camera_view)
        cubeView = findViewById(R.id.cube_view)
        mediaControl = findViewById(R.id.media_control)
        
        // Action buttons
        btnReset = findViewById(R.id.btn_reset)
        btnSolve = findViewById(R.id.btn_solve)
        
        // Initialize cube view với default cube
        // cubeView.createDefaultCube()
        
        // Show media control for testing - để xem giao diện
        mediaControl.visibility = View.VISIBLE
        mediaControl.setTotalSteps(20) // Giả lập 20 bước
        mediaControl.setCurrentStep(0)
        
        // Setup media control listener
        mediaControl.setOnControlListener(object : MediaControlView.OnControlListener {
            override fun onPlay() {
                currentState = AppState.ANIMATING
                updateUI()
                Toast.makeText(this@MainActivity, "Bắt đầu animation", Toast.LENGTH_SHORT).show()
            }
            
            override fun onPause() {
                currentState = AppState.SOLUTION_READY
                updateUI()
                Toast.makeText(this@MainActivity, "Tạm dừng animation", Toast.LENGTH_SHORT).show()
            }
            
            override fun onPrevious() {
                val currentStep = mediaControl.getCurrentStep()
                if (currentStep > 0) {
                    mediaControl.setCurrentStep(currentStep - 1)
                    // TODO: Implement step backward animation
                }
            }
            
            override fun onNext() {
                val currentStep = mediaControl.getCurrentStep()
                if (currentStep < mediaControl.getTotalSteps() - 1) {
                    mediaControl.setCurrentStep(currentStep + 1)
                    // TODO: Implement step forward animation
                }
            }
            
            override fun onSeek(position: Int) {
                mediaControl.setCurrentStep(position)
                // TODO: Implement seek to specific step
            }
            
            override fun onFirst() {
                mediaControl.setCurrentStep(0)
                // TODO: Implement go to first step
            }
            
            override fun onLast() {
                mediaControl.setCurrentStep(mediaControl.getTotalSteps() - 1)
                // TODO: Implement go to last step
            }
            
            override fun onFullscreen() {
                // TODO: Implement fullscreen logic
            }
            
            override fun onSpeedChanged(speed: MediaControlView.AnimationSpeed) {
                // TODO: Apply speed to animation
            }
            
            override fun onResetView() {
                // TODO: Reset cube view to default position
                // cubeView.resetView()
            }
        })
        
        // Test button để thử nghiệm cube animation
        // Có thể thêm test button vào layout nếu cần
        // (Hoặc có thể trigger thông qua debug menu)
    }
    
    /**
     * Method test để thử nghiệm cube animation
     */
    private fun testCubeAnimation() {
        // Test một sequence moves đơn giản
        val testMoves = "R U R' U' R U R' U'"
        // cubeView.doMoveFromString(testMoves)
        Toast.makeText(this, "Đang thực hiện test moves: $testMoves", Toast.LENGTH_SHORT).show()
    }
    
    private fun setupEventListeners() {
        btnReset.setOnClickListener { resetApp() }
        btnSolve.setOnClickListener { solveCube() }
        
        // Thêm capture button functionality
        cameraView.setOnClickListener {
            when (currentState) {
                AppState.SCAN_READY -> {
                    // Bắt đầu quét
                    currentState = AppState.SCANNING
                    updateUI()
                    cameraView.resetDetection()
                    Toast.makeText(this, "Đang quét... Hãy đặt cube vào khung", Toast.LENGTH_SHORT).show()
                }
                AppState.SCANNING -> {
                    // Capture colors manually
                    cameraView.captureColors()
                }
                else -> {
                    // Do nothing for other states
                }
            }
        }
        
        // Test cube animation - long click on cube view
        cubeView.setOnLongClickListener {
            testCubeAnimation()
            true
        }
    }
    
    // Phương thức test để simulate scan complete
    private fun simulateScanComplete() {
        if (currentState == AppState.SCAN_READY) {
            currentState = AppState.SCANNING
            updateUI()
            
            // Simulate scanning process
            Thread {
                try {
                    Thread.sleep(1000) // 1 giây
                    runOnUiThread {
                        currentState = AppState.SCAN_COMPLETE
                        updateUI()
                        Toast.makeText(this, "Quét hoàn tất! Nhấn 'Giải Cube'", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
    
    private fun resetApp() {
        currentState = AppState.SCAN_READY
        mediaControl.visibility = View.GONE
        
        // Reset camera
        cameraView.resetDetection()
        
        updateUI()
        Toast.makeText(this, "Đã reset! Bắt đầu quét lại cube.", Toast.LENGTH_SHORT).show()
    }
    
    private fun solveCube() {
        if (currentState == AppState.SCAN_COMPLETE) {
            currentState = AppState.SOLVING
            updateUI()
            
            // Simulate solving process
            Thread {
                try {
                    Thread.sleep(2000) // Simulate calculation time
                    runOnUiThread {
                        currentState = AppState.SOLUTION_READY
                        mediaControl.visibility = View.VISIBLE
                        mediaControl.setTotalSteps(20) // Giả lập 20 bước
                        mediaControl.setCurrentStep(0)
                        updateUI()
                        Toast.makeText(this, "Đã tính xong! Nhấn Play để xem animation", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
    
    private fun updateUI() {
        when (currentState) {
            AppState.SCAN_READY -> {
                statusView.setStatus("Đặt cube vào khung và bắt đầu quét", StatusView.ScanStatus.IDLE)
                btnSolve.isEnabled = false
            }
            AppState.SCANNING -> {
                statusView.setStatus("Đang quét cube...", StatusView.ScanStatus.SCANNING)
                btnSolve.isEnabled = false
            }
            AppState.SCAN_COMPLETE -> {
                statusView.setStatus("Quét hoàn tất! Nhấn 'Giải' để bắt đầu", StatusView.ScanStatus.ALL_COMPLETE)
                btnSolve.isEnabled = true
            }
            AppState.SOLVING -> {
                statusView.setStatus("Đang tính toán cách giải...", StatusView.ScanStatus.SOLVING)
                btnSolve.isEnabled = false
            }
            AppState.SOLUTION_READY -> {
                statusView.setStatus("Sẵn sàng! 20 bước để giải", StatusView.ScanStatus.SOLVED)
                btnSolve.isEnabled = false
            }
            AppState.ANIMATING -> {
                statusView.setStatus("Đang thực hiện giải cube...", StatusView.ScanStatus.SOLVING)
                btnSolve.isEnabled = false
            }
            AppState.COMPLETED -> {
                statusView.setStatus("Đã giải xong! Chúc mừng!", StatusView.ScanStatus.SOLVED)
                btnSolve.isEnabled = false
            }
        }
    }
}
