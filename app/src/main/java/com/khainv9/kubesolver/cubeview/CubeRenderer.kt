package com.khainv9.kubesolver.cubeview

import android.animation.ValueAnimator
import android.opengl.GLES20
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import androidx.core.animation.addListener
import com.khainv9.kubesolver.cubeview.Direction
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

enum class Step {
    Step_Top, // First face
    Step_Left, // Do LeftToRight
    Step_Back, // Do TopToBottom
    Step_Right, // Do TopToBottom
    Step_Front, // Do TopToBottom
    Step_Bottom;

    fun getNextStep(): Step {
        return when (this) {
            Step_Top -> Step_Left
            Step_Left -> Step_Back
            Step_Back -> Step_Right
            Step_Right -> Step_Front
            Step_Front -> Step_Bottom
            Step_Bottom -> Step_Top
        }
    }
}

class RubiksCubeRenderer : GLSurfaceView.Renderer {
    var angleX = 20f
    var angleY = -20f
    var angleZ = 0f
    var targetAngleX = angleX
    var targetAngleY = angleY
    var targetAngleZ = angleZ

    var pendingMoves = mutableListOf<Move>()

    // Trạng thái của Rubik; có thể cập nhật từ ngoài nếu cần.
    private val colorfulCube = ColorfulCube()

    private lateinit var cube: Cube3D

    // Các ma trận biến đổi
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        for (i in 0..5)  {
            for (j in 0..8) {
                colorfulCube.colors[i][j] = Face.entries[i].getInitColor()
//                colorfulCube.colors[i][j] = CubeColor.UNKNOWN
            }
        }
        cube = Cube3D(colorfulCube)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    fun rotate(direction: Direction, timeAnimateMs: Int) {
        when (direction) { Direction.LeftToRight -> targetAngleY -= 90f
            Direction.RightToLeft -> targetAngleY += 90f
            Direction.TopToBottom -> targetAngleX -= 90f
            Direction.BottomToTop -> {
                targetAngleX += 90f
                targetAngleY = 0f
                targetAngleZ = 20f


//                targetAngleY = - targetAngleY
            }
            else -> {}
        }
        startRotationAnimation(timeAnimateMs)
    }

    fun rotateByFace(face: Face) {
        val offsetAngle = 20f
        when (face) {
            Face.FRONT -> {
                targetAngleX = offsetAngle
                targetAngleY = -offsetAngle
                targetAngleZ = 0f
            }
            Face.LEFT -> {
                targetAngleX = offsetAngle
                targetAngleY = 90f - offsetAngle
                targetAngleZ = 0f
            }
            Face.BACK -> {
                targetAngleX = offsetAngle
                targetAngleY = 180 - offsetAngle
                targetAngleZ = 0f
            }
            Face.RIGHT -> {
                targetAngleX = offsetAngle
                targetAngleY = -90f
                targetAngleZ = 0f
            }
            Face.UP -> {
                targetAngleX = 90 + offsetAngle
                targetAngleY = 0f
                targetAngleZ = offsetAngle
            }
            Face.DOWN -> {
                targetAngleX = -90 + offsetAngle
                targetAngleY = 0f
                targetAngleZ = -offsetAngle
            }
        }
        startRotationAnimation(500)
    }

    fun resetRotation() {
        rotateByFace(Face.FRONT)
    }

    fun startRotationAnimation(duration: Int) {
        val animatorX = ValueAnimator.ofFloat(angleX, targetAngleX)
        animatorX.addUpdateListener { animation ->
            angleX = animation.animatedValue as Float
        }
        animatorX.duration = duration.toLong()
        animatorX.start()

        val animatorY = ValueAnimator.ofFloat(angleY, targetAngleY)
        animatorY.addUpdateListener { animation ->
            angleY = animation.animatedValue as Float
        }
        animatorY.duration = duration.toLong()
        animatorY.start()

        val animatorZ = ValueAnimator.ofFloat(angleZ, targetAngleZ)
        animatorZ.addUpdateListener { animation ->
            angleZ = animation.animatedValue as Float
        }
        animatorZ.duration = duration.toLong()
        animatorZ.start()
    }

    fun rotateLeft(direction: Direction, timeAnimateMs: Int) {
        val rotationAngles = when (direction) {
            Direction.LeftToRight -> Pair(0f, -90f)
            Direction.RightToLeft -> Pair(0f, 90f)
            Direction.TopToBottom -> Pair(-90f, 0f)
            Direction.BottomToTop -> Pair(90f, 0f)
            else -> Pair(0f, 0f)
        }

        val animatorX = ValueAnimator.ofFloat(angleX, angleX + rotationAngles.first)
        animatorX.addUpdateListener { animation ->
            angleX = animation.animatedValue as Float
        }
        animatorX.duration = timeAnimateMs.toLong()
        animatorX.start()

        val animatorY = ValueAnimator.ofFloat(angleY, angleY + rotationAngles.second)
        animatorY.addUpdateListener { animation ->
            angleY = animation.animatedValue as Float
        }
        animatorY.duration = timeAnimateMs.toLong()
        animatorY.start()

        when(direction) {
            else -> {
            }
        }
    }

    /**
     * Cập nhật cube với trạng thái mới
     */
    fun updateCube(colorfulCube: ColorfulCube) {
        this.colorfulCube.colors = colorfulCube.colors
        if (::cube.isInitialized) {
            cube.updateCube(colorfulCube)
        }
    }

    /**
     * Lấy trạng thái hiện tại của cube
     */
    fun getCubeState(): ColorfulCube {
        return if (::cube.isInitialized) {
            cube.getCubeState()
        } else {
            colorfulCube
        }
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(unused: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, angleX, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, angleZ, 0f, 0f, 1f)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
        cube.draw(mvpMatrix)
    }

    /**
     * Thực hiện move với animation
     */
    fun startDoMove(move: Move, duration: Int) {
        // Thêm move vào queue để thực hiện tuần tự
        pendingMoves.add(move)
        
        // Nếu đây là move đầu tiên trong queue, bắt đầu animation
        if (pendingMoves.size == 1) {
            executeNextMove(duration)
        }
    }
    
    /**
     * Thực hiện move tiếp theo trong queue
     */
    private fun executeNextMove(duration: Int) {
        if (pendingMoves.isEmpty()) return
        
        val move = pendingMoves.first()
        
        // Tạo animation cho move
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Float
            // Cập nhật animation progress tại đây
            // Có thể cần implement logic animation cho từng move cụ thể
        }
        
        animator.addListener(
            onEnd = {
                // Áp dụng move vào cube state
                applyMoveToState(move)
                
                // Xóa move đã hoàn thành
                pendingMoves.removeFirst()
                
                // Thực hiện move tiếp theo nếu còn
                if (pendingMoves.isNotEmpty()) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        executeNextMove(duration)
                    }, 50) // Delay nhỏ giữa các move
                }
            }
        )
        
        animator.duration = duration.toLong()
        animator.start()
    }
    
    /**
     * Áp dụng move vào trạng thái cube
     */
    private fun applyMoveToState(move: Move) {
        // Implement logic để áp dụng move vào colorfulCube
        // Và cập nhật cube 3D
        when (move) {
            Move.U -> {
                // Rotate mặt trên clockwise
                rotateFaceClockwise(Face.UP)
            }
            Move.U_ -> {
                // Rotate mặt trên counter-clockwise
                rotateFaceCounterClockwise(Face.UP)
            }
            Move.U2 -> {
                // Rotate mặt trên 180 độ
                rotateFaceClockwise(Face.UP)
                rotateFaceClockwise(Face.UP)
            }
            Move.D -> rotateFaceClockwise(Face.DOWN)
            Move.D_ -> rotateFaceCounterClockwise(Face.DOWN)
            Move.D2 -> {
                rotateFaceClockwise(Face.DOWN)
                rotateFaceClockwise(Face.DOWN)
            }
            Move.L -> rotateFaceClockwise(Face.LEFT)
            Move.L_ -> rotateFaceCounterClockwise(Face.LEFT)
            Move.L2 -> {
                rotateFaceClockwise(Face.LEFT)
                rotateFaceClockwise(Face.LEFT)
            }
            Move.R -> rotateFaceClockwise(Face.RIGHT)
            Move.R_ -> rotateFaceCounterClockwise(Face.RIGHT)
            Move.R2 -> {
                rotateFaceClockwise(Face.RIGHT)
                rotateFaceClockwise(Face.RIGHT)
            }
            Move.F -> rotateFaceClockwise(Face.FRONT)
            Move.F_ -> rotateFaceCounterClockwise(Face.FRONT)
            Move.F2 -> {
                rotateFaceClockwise(Face.FRONT)
                rotateFaceClockwise(Face.FRONT)
            }
            Move.B -> rotateFaceClockwise(Face.BACK)
            Move.B_ -> rotateFaceCounterClockwise(Face.BACK)
            Move.B2 -> {
                rotateFaceClockwise(Face.BACK)
                rotateFaceClockwise(Face.BACK)
            }
        }
        
        // Cập nhật cube 3D với trạng thái mới
        if (::cube.isInitialized) {
            cube.updateCube(colorfulCube)
        }
    }
    
    /**
     * Rotate một mặt clockwise
     */
    private fun rotateFaceClockwise(face: Face) {
        val faceIndex = face.ordinal
        val colors = colorfulCube.colors[faceIndex]
        
        // Lưu trữ các góc
        val temp = colors[0]
        colors[0] = colors[6]
        colors[6] = colors[8]
        colors[8] = colors[2]
        colors[2] = temp
        
        // Lưu trữ các cạnh
        val temp2 = colors[1]
        colors[1] = colors[3]
        colors[3] = colors[7]
        colors[7] = colors[5]
        colors[5] = temp2
        
        // Cần rotate các cạnh liền kề nữa, nhưng logic này khá phức tạp
        // Tạm thời implement cơ bản
    }
    
    /**
     * Rotate một mặt counter-clockwise
     */
    private fun rotateFaceCounterClockwise(face: Face) {
        // Rotate clockwise 3 lần = counter-clockwise 1 lần
        rotateFaceClockwise(face)
        rotateFaceClockwise(face)
        rotateFaceClockwise(face)
    }
}
