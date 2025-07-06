package com.khainv9.kubesolver.camera

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.khainv9.kubesolver.cubeview.CubeColor
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Core
import org.opencv.imgproc.Imgproc
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.PI

/**
 * EnhancedCameraView - Camera view thực tế sử dụng OpenCV
 * Tương tự như trong CameraActivity nhưng được tối ưu cho MainActivity
 */
class EnhancedCameraView : JavaCameraView, CameraBridgeViewBase.CvCameraViewListener2 {
    
    companion object {
        private const val TAG = "EnhancedCameraView"
    }
    
    private lateinit var colorDetector: ColorDetector
    private lateinit var detectedColors: Array<CubeColor>
    private var showScanOverlay = true
    
    // Các Mat objects tái sử dụng để tránh memory leak
    private var croppedSquare: Mat? = null
    private var rotatedSquare: Mat? = null
    private var newFrame: Mat? = null
    
    // Flag để tránh callback liên tục
    private var hasNotifiedScan = false
    
    // Callback interface để thông báo khi scan xong
    interface OnFaceScannedListener {
        fun onFaceScanned(colors: Array<CubeColor>)
    }
    
    // Callback interface để thông báo tiến trình scan
    interface OnScanProgressListener {
        fun onScanProgress(colors: Array<CubeColor>, detectedCount: Int, totalCount: Int)
    }

    private var scanListener: OnFaceScannedListener? = null
    private var progressListener: OnScanProgressListener? = null
    
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        colorDetector = ColorDetector()
        detectedColors = Array(9) { CubeColor.UNKNOWN }
        setCvCameraViewListener(this)
    }
    
    // Setter cho callback
    fun setOnFaceScannedListener(listener: OnFaceScannedListener?) {
        this.scanListener = listener
    }
    
    // Setter cho progress callback
    fun setOnScanProgressListener(listener: OnScanProgressListener?) {
        this.progressListener = listener
    }
    
    fun setShowScanOverlay(show: Boolean) {
        this.showScanOverlay = show
    }
    
    fun getDetectedColors(): Array<CubeColor> {
        return detectedColors.clone()
    }
    
    fun isAllColorsDetected(): Boolean {
        return detectedColors.all { it != CubeColor.UNKNOWN }
    }
    
    // Implement CvCameraViewListener2
    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.d(TAG, "Camera view started: ${width}x$height")
    }

    override fun onCameraViewStopped() {
        Log.d(TAG, "Camera view stopped")
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        
        // Lazy initialization của Mat objects
        if (croppedSquare == null) {
            croppedSquare = Mat()
            rotatedSquare = Mat()
            newFrame = Mat()
        }
        
        // Lưu lại kích thước gốc
        val originalWidth = frame.width()
        val originalHeight = frame.height()
        
        // Crop lấy vùng hình vuông bên phía trái với kích thước height x height
        val squareSize = originalHeight
        val cropRect = org.opencv.core.Rect(0, 0, squareSize, squareSize)
        
        // Sử dụng Mat đã có sẵn thay vì tạo mới
        frame.submat(cropRect).copyTo(croppedSquare!!)
        
        // Xoay hình vuông chiều kim đồng hồ (90 độ)
        Core.rotate(croppedSquare!!, rotatedSquare!!, Core.ROTATE_90_CLOCKWISE)
        
        // Tạo frame mới có kích thước bằng frame input
        newFrame!!.create(originalHeight, originalWidth, frame.type())
        newFrame!!.setTo(Scalar(0.0, 0.0, 0.0, 0.0)) // Làm trong suốt
        
        // Copy hình vuông đã xoay vào newFrame ở vị trí phía trái
        val roiRect = org.opencv.core.Rect(0, 0, squareSize, squareSize)
        val roi = newFrame!!.submat(roiRect)
        rotatedSquare!!.copyTo(roi)
        roi.release()
        
        // Thực hiện processCubeDetection trên newFrame
        if (showScanOverlay) {
            processCubeDetection(newFrame!!)
        }
        
        return newFrame!!.clone()
    }
    
    private fun processCubeDetection(frame: Mat) {
        val width = frame.width()
        val height = frame.height()
        val size = min(width, height)
        val cubePreferSize = size * 4 / 6

        // Tính toán vị trí khung quét (tương tự CameraActivity)
        var paddingLeft = (width - size) / 2
        if (paddingLeft < 0) paddingLeft = 0

        val faceVertex = Array(4) { Point() }
        val centerX = paddingLeft + (cubePreferSize / 2.0)
        val centerY = height / 2.0
        val center = Point(centerX, centerY)

        // Tạo 4 góc của khung quét
        for (i in 0 until 4) {
            val angle = Math.toRadians(45 - i * 90.0)
            faceVertex[i] = Point(
                centerX + cubePreferSize * cos(angle),
                centerY - cubePreferSize * sin(angle)
            )
        }

        // Detect màu sắc
        val quadrilaterals = getAllQuadrilaterals(faceVertex[0], faceVertex[1], faceVertex[2], faceVertex[3])
        val cubeColors = colorDetector.processFrame(frame, quadrilaterals)
        
        // Cập nhật màu sắc đã detect
        for (i in quadrilaterals.indices) {
            val quad = quadrilaterals[i]
            if (i < cubeColors.size) {
                detectedColors[quad.hIndex * 3 + quad.vIndex] = cubeColors[i]
            }
        }

        // Vẽ overlay
        drawScanOverlay(frame, faceVertex, center)
        
        // Thông báo tiến trình scan (luôn gửi kết quả)
        val detectedCount = detectedColors.count { it != CubeColor.UNKNOWN }
        progressListener?.onScanProgress(getDetectedColors(), detectedCount, 9)
        
        // Thông báo khi scan xong (chỉ thông báo 1 lần)
        if (isAllColorsDetected() && !hasNotifiedScan) {
            hasNotifiedScan = true
            scanListener?.onFaceScanned(getDetectedColors())
        }
    }
    
    private fun drawScanOverlay(frame: Mat, faceVertex: Array<Point>, center: Point) {
        // Vẽ khung quét
        val borderColor = Scalar(0.0, 255.0, 0.0, 255.0) // Xanh lá
        for (i in 0 until 4) {
            Imgproc.line(frame, faceVertex[i], faceVertex[(i + 1) % 4], borderColor, 3)
        }

        // Vẽ lưới 3x3
        drawGrid(frame, faceVertex[0], faceVertex[1], faceVertex[2], faceVertex[3])
        
        // Vẽ các ô màu đã detect
        drawDetectedColors(frame, faceVertex[0], faceVertex[1], faceVertex[2], faceVertex[3])
    }
    
    private fun drawGrid(frame: Mat, p1: Point, p2: Point, p3: Point, p4: Point) {
        val numGrid = 3
        
        // Tính toán khoảng cách giữa các đường lưới
        val horizontalStepX = (p2.x - p1.x) / numGrid
        val horizontalStepY = (p2.y - p1.y) / numGrid
        val verticalStepX = (p4.x - p1.x) / numGrid
        val verticalStepY = (p4.y - p1.y) / numGrid

        val gridColor = Scalar(255.0, 255.0, 255.0, 255.0) // Trắng
        val gridWidth = 2
        
        // Vẽ đường lưới ngang
        for (i in 1 until numGrid) {
            val start = Point(p1.x + verticalStepX * i, p1.y + verticalStepY * i)
            val end = Point(start.x + horizontalStepX * numGrid, start.y + horizontalStepY * numGrid)
            Imgproc.line(frame, start, end, gridColor, gridWidth)
        }

        // Vẽ đường lưới dọc
        for (i in 1 until numGrid) {
            val start = Point(p1.x + horizontalStepX * i, p1.y + horizontalStepY * i)
            val end = Point(start.x + verticalStepX * numGrid, start.y + verticalStepY * numGrid)
            Imgproc.line(frame, start, end, gridColor, gridWidth)
        }
    }
    
    private fun drawDetectedColors(frame: Mat, p1: Point, p2: Point, p3: Point, p4: Point) {
        val numGrid = 3
        
        // Tính toán khoảng cách
        val horizontalStepX = (p2.x - p1.x) / numGrid
        val horizontalStepY = (p2.y - p1.y) / numGrid
        val verticalStepX = (p4.x - p1.x) / numGrid
        val verticalStepY = (p4.y - p1.y) / numGrid

        val factor = 0.2 // Kích thước của ô màu
        
        for (hIndex in 0 until numGrid) {
            for (vIndex in 0 until numGrid) {
                val color = detectedColors[hIndex * 3 + vIndex]
                
                // Tính toán vị trí các góc của ô màu
                val pp = Point(
                    p1.x + verticalStepX * hIndex + horizontalStepX * vIndex,
                    p1.y + verticalStepY * hIndex + horizontalStepY * vIndex
                )
                val ppx = Point(
                    pp.x + verticalStepX * factor,
                    pp.y + verticalStepY * factor
                )
                val ppy = Point(
                    pp.x + horizontalStepX * factor,
                    pp.y + horizontalStepY * factor
                )
                val ppxy = Point(
                    pp.x + verticalStepX * factor + horizontalStepX * factor,
                    pp.y + verticalStepY * factor + horizontalStepY * factor
                )
                
                // Vẽ ô màu
                val points = MatOfPoint(pp, ppx, ppxy, ppy)
                Imgproc.fillPoly(frame, listOf(points), getColorScalar(color))
            }
        }
    }
    
    private fun getColorScalar(color: CubeColor): Scalar {
        return when (color) {
            CubeColor.RED -> Scalar(255.0, 0.0, 0.0, 255.0)
            CubeColor.BLUE -> Scalar(0.0, 0.0, 255.0, 255.0)
            CubeColor.WHITE -> Scalar(255.0, 255.0, 255.0, 255.0)
            CubeColor.GREEN -> Scalar(0.0, 255.0, 0.0, 255.0)
            CubeColor.YELLOW -> Scalar(255.0, 255.0, 0.0, 255.0)
            CubeColor.ORANGE -> Scalar(255.0, 165.0, 0.0, 255.0)
            else -> Scalar(128.0, 128.0, 128.0, 255.0) // Xám cho UNKNOWN
        }
    }
    
    private fun getAllQuadrilaterals(p1: Point, p2: Point, p3: Point, p4: Point): List<Quadrilateral> {
        val quadrilaterals = mutableListOf<Quadrilateral>()
        val numGrid = 3
        
        // Tính toán khoảng cách
        val horizontalStepX = (p2.x - p1.x) / numGrid
        val horizontalStepY = (p2.y - p1.y) / numGrid
        val verticalStepX = (p4.x - p1.x) / numGrid
        val verticalStepY = (p4.y - p1.y) / numGrid

        for (hIndex in 0 until numGrid) {
            for (vIndex in 0 until numGrid) {
                val pp = Point(
                    p1.x + verticalStepX * hIndex + horizontalStepX * vIndex,
                    p1.y + verticalStepY * hIndex + horizontalStepY * vIndex
                )
                val ppx = Point(
                    pp.x + verticalStepX,
                    pp.y + verticalStepY
                )
                val ppy = Point(
                    pp.x + horizontalStepX,
                    pp.y + horizontalStepY
                )
                val ppxy = Point(
                    pp.x + verticalStepX + horizontalStepX,
                    pp.y + verticalStepY + horizontalStepY
                )
                
                quadrilaterals.add(Quadrilateral(pp, ppx, ppxy, ppy, hIndex, vIndex))
            }
        }
        return quadrilaterals
    }
    
    // Methods để tương thích với MainActivity
    override fun enableView() {
        super.enableView()
        Log.d(TAG, "Camera view enabled")
    }
    
    override fun disableView() {
        cleanup()
        super.disableView()
        Log.d(TAG, "Camera view disabled")
    }
    
    // Cleanup resources để tránh memory leak
    fun cleanup() {
        croppedSquare?.release()
        rotatedSquare?.release()
        newFrame?.release()
        croppedSquare = null
        rotatedSquare = null
        newFrame = null
    }
    
    // Reset detected colors
    fun resetDetection() {
        hasNotifiedScan = false
        for (i in detectedColors.indices) {
            detectedColors[i] = CubeColor.UNKNOWN
        }
    }
    
    // Manually capture current frame colors
    fun captureColors() {
        if (scanListener != null && isAllColorsDetected()) {
            scanListener?.onFaceScanned(getDetectedColors())
        }
    }
    
    // Get current scan progress
    fun getScanProgress(): Pair<Int, Int> {
        val detectedCount = detectedColors.count { it != CubeColor.UNKNOWN }
        return Pair(detectedCount, 9)
    }
    
    // Get completion percentage
    fun getCompletionPercentage(): Float {
        val detectedCount = detectedColors.count { it != CubeColor.UNKNOWN }
        return (detectedCount.toFloat() / 9.0f) * 100.0f
    }
}
