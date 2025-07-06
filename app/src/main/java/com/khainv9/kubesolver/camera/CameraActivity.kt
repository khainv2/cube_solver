package com.khainv9.kubesolver.camera

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.khainv9.kubesolver.R
import com.khainv9.kubesolver.cubeview.CubeColor
import com.khainv9.kubesolver.cubeview.Direction
import com.khainv9.kubesolver.cubeview.Face
import com.khainv9.kubesolver.cubeview.Move
import com.khainv9.kubesolver.cubeview.RubiksCubeGLSurfaceView
import com.khainv9.kubesolver.cubeview.Step
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.PI

class CameraActivity : Activity(), CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    
    companion object {
        private const val TAG = "CameraActivity"
    }

    private var readyToNext = false
    private lateinit var colors: Array<CubeColor>
    private lateinit var phaseControl: PhaseControl
    private lateinit var cameraView: CameraBridgeViewBase
    private lateinit var cubeView: RubiksCubeGLSurfaceView
    private var step = Step.Step_Top
    private val detector = ColorDetector()

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    cameraView.enableView()
                    cameraView.setOnTouchListener(this@CameraActivity)
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    init {
        Log.i(TAG, "Instantiated new ${this.javaClass}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera)

        phaseControl = PhaseControl()

        cameraView = findViewById(R.id.camera_view)
        cameraView.visibility = SurfaceView.VISIBLE
        cameraView.setCvCameraViewListener(this)

        val btReset = findViewById<Button>(R.id.bt_Reset)
        val btNext = findViewById<Button>(R.id.bt_Next)
        val btCapture = findViewById<Button>(R.id.bt_Capture)
        val btTest = findViewById<Button>(R.id.bt_Test)
        val btTest2 = findViewById<Button>(R.id.bt_Test2)
        val btTest3 = findViewById<Button>(R.id.bt_Test3)
        val btTest4 = findViewById<Button>(R.id.bt_Test4)
        val btTest5 = findViewById<Button>(R.id.bt_Test5)
        val btTest6 = findViewById<Button>(R.id.bt_Test6)
        val tvInstruction = findViewById<TextView>(R.id.tv_Instruction)
        phaseControl.reset()

        // tvInstruction.text = "Please capture the top face of the cube"
        btCapture.setOnClickListener {
            if (!isAllKnown()) {
                Toast.makeText(this, "Please capture all colors", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            readyToNext = true
            phaseControl.updateColor(colors, Rotation.Deg_0)
            cubeView.updateCube(phaseControl.toColorfulCube())
        }

        btReset.setOnClickListener {
            readyToNext = false
            phaseControl.reset()
            cubeView.resetRotation()
            // cubeView.updateCube(phaseControl.toColorfulCube())
        }

        btNext.setOnClickListener {
            readyToNext = false
            cubeView.rotate(phaseControl.getSuggestDirection())
            phaseControl.stepNext()
        }

        btTest.setOnClickListener {
            val str = "U' F2 L2 B2 F2 U' F2 R2 U' L2 D2 L2 F' D' R' F' R B' U' B2 L'"
            val moves = Move.parseMoveList(str)
            for (i in moves.indices) {
                val move = moves[i]
                cubeView.renderer.startDoMove(move, 300)
            }
        }

        btTest2.setOnClickListener {
            cubeView.renderer.rotateByFace(Face.LEFT)
        }
        btTest3.setOnClickListener {
            cubeView.renderer.rotateByFace(Face.BACK)
        }

        btTest4.setOnClickListener {
            cubeView.renderer.rotateByFace(Face.RIGHT)
        }
        btTest5.setOnClickListener {
            cubeView.renderer.rotateByFace(Face.UP)
        }
        btTest6.setOnClickListener {
            cubeView.renderer.rotateByFace(Face.DOWN)
        }

        cubeView = findViewById(R.id.cube_view)

        colors = Array(9) { CubeColor.UNKNOWN }
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()

        val width = frame.width()
        val height = frame.height()
        val size = min(width, height)
        val cubePreferSize = size * 4 / 6

        // Calculate hexagon points
        var paddingLeft = (width - size) / 2
        if (paddingLeft < 0) paddingLeft = 0

        val faceVertex = Array(4) { Point() }
        val centerX = paddingLeft + (cubePreferSize / 2.0)
        val centerY = height / 2.0
        val center = Point(centerX, centerY)

        for (i in 0 until 4) {
            val angle = Math.toRadians(45 - i * 90.0) // Adjusted starting angle
            faceVertex[i] = Point(
                centerX + cubePreferSize * cos(angle),
                centerY - cubePreferSize * sin(angle)
            ) // Adjusted y-coordinate
        }

        val quadrilaterals = getAllQuad(faceVertex[0], faceVertex[1], faceVertex[2], faceVertex[3])
        val cubeColors = detector.processFrame(frame, quadrilaterals)
        for (i in quadrilaterals.indices) {
            val quad = quadrilaterals[i]
            colors[quad.hIndex * 3 + quad.vIndex] = cubeColors[i]
        }

        // Draw hexagon
        val borderColor = Scalar(0.0, 255.0, 0.0)
        val vertexColor = Scalar(0.0, 0.0, 255.0)
        val gridColor = Scalar(60.0, 60.0, 60.0)
        for (i in 0 until 4) {
            Imgproc.line(frame, faceVertex[i], faceVertex[(i + 1) % 4], borderColor, 3)
        }

        // Vẽ lần lượt các đường nối bên trong
        drawGrid(frame, faceVertex[0], faceVertex[1], faceVertex[2], faceVertex[3])

        drawSmallCorner(frame, faceVertex[0], faceVertex[1], faceVertex[2], faceVertex[3])

        if (readyToNext) {
            drawArrowDirectionOnMat(frame, center, phaseControl.getSuggestDirection())
        }

        return frame
    }

    private fun isAllKnown(): Boolean {
        return colors.all { it != CubeColor.UNKNOWN }
    }

    private fun drawArrowDirectionOnMat(frame: Mat, center: Point, direction: Direction) {
        val width = frame.cols()
        val height = frame.rows()
        val side = min(width, height).toDouble()
        val arrowLength = 0.6 * side
        val arrowTipSize = 15 // 80 dp
        val arrowColor = Scalar(33.0, 255.0, 123.0)

        val (start, end) = when (direction) {
            Direction.LeftToRight -> {
                val start = Point((width - arrowLength) / 2, height / 2.0)
                val end = Point(start.x + arrowLength, start.y)
                Pair(start, end)
            }
            Direction.RightToLeft -> {
                val start = Point((width + arrowLength) / 2, height / 2.0)
                val end = Point(start.x - arrowLength, start.y)
                Pair(start, end)
            }
            Direction.TopToBottom -> {
                val start = Point(width / 2.0, (height - arrowLength) / 2)
                val end = Point(start.x, start.y + arrowLength)
                Pair(start, end)
            }
            Direction.BottomToTop -> {
                val start = Point(width / 2.0, (height + arrowLength) / 2)
                val end = Point(start.x, start.y - arrowLength)
                Pair(start, end)
            }
            else -> return
        }

        start.x += center.x - width / 2
        end.x += center.x - width / 2
        Imgproc.arrowedLine(frame, start, end, arrowColor, 30, Imgproc.LINE_4, 0, 0.5)
    }

    private fun getColorByCubeColor(color: CubeColor): Scalar {
        return when (color) {
            CubeColor.RED -> Scalar(255.0, 0.0, 0.0)
            CubeColor.BLUE -> Scalar(0.0, 0.0, 255.0)
            CubeColor.WHITE -> Scalar(255.0, 255.0, 255.0)
            CubeColor.GREEN -> Scalar(0.0, 255.0, 0.0)
            CubeColor.YELLOW -> Scalar(255.0, 255.0, 0.0)
            CubeColor.ORANGE -> Scalar(255.0, 165.0, 0.0)
            else -> Scalar(128.0, 128.0, 128.0)
        }
    }

    private fun drawGrid(frame: Mat, p1: Point, p2: Point, p3: Point, p4: Point) {
        val numGrid = 3
        // Calculate step sizes for horizontal and vertical lines
        val horizontalStepX = (p2.x - p1.x) / numGrid
        val horizontalStepY = (p2.y - p1.y) / numGrid
        val verticalStepX = (p4.x - p1.x) / numGrid
        val verticalStepY = (p4.y - p1.y) / numGrid

        val gridColor = Scalar(255.0, 255.0, 255.0)
        val gridWidth = 1
        // Draw horizontal grid lines
        for (i in 1 until numGrid) {
            val start = Point(p1.x + verticalStepX * i, p1.y + verticalStepY * i)
            val end = Point(start.x + horizontalStepX * numGrid, start.y + horizontalStepY * numGrid)
            Imgproc.line(frame, start, end, gridColor, gridWidth)
        }

        // Draw vertical grid lines
        for (i in 1 until numGrid) {
            val start = Point(p1.x + horizontalStepX * i, p1.y + horizontalStepY * i)
            val end = Point(start.x + verticalStepX * numGrid, start.y + verticalStepY * numGrid)
            Imgproc.line(frame, start, end, gridColor, gridWidth)
        }

        // int channels = frame.channels();
        // Log.d("MyLog", "Channel: " +  CvType.typeToString(frame.type()));
    }

    private fun drawSmallCorner(frame: Mat, p1: Point, p2: Point, p3: Point, p4: Point): Mat {
        val numGrid = 3

        // Calculate step sizes for horizontal and vertical lines
        val horizontalStepX = (p2.x - p1.x) / numGrid
        val horizontalStepY = (p2.y - p1.y) / numGrid
        val verticalStepX = (p4.x - p1.x) / numGrid
        val verticalStepY = (p4.y - p1.y) / numGrid

        val factor = 0.2
        for (hIndex in 0 until numGrid) {
            // Draw vertical grid lines
            for (vIndex in 0 until numGrid) {
                val color = colors[hIndex * 3 + vIndex]
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
                val points = MatOfPoint(pp, ppx, ppxy, ppy)
                Imgproc.fillPoly(frame, listOf(points), getColorByCubeColor(color))
            }
        }
        return frame
    }

    private fun getAllQuad(p1: Point, p2: Point, p3: Point, p4: Point): List<Quadrilateral> {
        val quadrilaterals = mutableListOf<Quadrilateral>()
        val numGrid = 3
        // Calculate step sizes for horizontal and vertical lines
        val horizontalStepX = (p2.x - p1.x) / numGrid
        val horizontalStepY = (p2.y - p1.y) / numGrid
        val verticalStepX = (p4.x - p1.x) / numGrid
        val verticalStepY = (p4.y - p1.y) / numGrid

        for (hIndex in 0 until numGrid) {
            // Draw vertical grid lines
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
                quadrilaterals.add(
                    Quadrilateral(pp, ppx, ppxy, ppy, hIndex, vIndex)
                )
            }
        }
        return quadrilaterals
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return false
    }
}
