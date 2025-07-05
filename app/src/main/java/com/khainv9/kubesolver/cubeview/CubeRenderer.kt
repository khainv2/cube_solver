package com.khainv9.kubesolver.cubeview

import android.animation.ValueAnimator
import android.opengl.GLES20
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import androidx.core.animation.addListener
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

enum class Direction {
    NoDirection, LeftToRight, RightToLeft, TopToBottom, BottomToTop
}
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

    fun updateCube(cubeState: ColorfulCube) {
        cube.updateCube(cubeState)
    }

    fun startDoMove(move: Move, timeAnimateMs: Int) {
        if (timeAnimateMs == 0) {
            val c = cube.getCubeState()
            c.updateByMove(move)
            cube.updateCube(c)
            return
        }
        if (cube.move != null) {
            pendingMoves.add(move)
            return
        }

        cube.move = move
        var destinationAngle = 0.0f
        if (move.isClockwise()) {
            destinationAngle = -90.0f
        } else if (move.isAntiClockwise()) {
            destinationAngle = 90.0f
        } else if (move.isDouble()) {
            destinationAngle = 180.0f
        }
        if (move.getFace() == Face.DOWN || move.getFace() == Face.LEFT || move.getFace() == Face.BACK) {
            destinationAngle = -destinationAngle
        }
        val animator = ValueAnimator.ofFloat(0f, destinationAngle)
        animator.addUpdateListener { animation ->
            cube.angle = animation.animatedValue as Float
        }
        // Set on animation end
        animator.addListener(onEnd = {
            cube.move = null
            val c = cube.getCubeState()
            c.updateByMove(move)
            cube.updateCube(c)

            // Create delay timer
            if (pendingMoves.isNotEmpty()) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val next = pendingMoves[0]
                    startDoMove(next, timeAnimateMs)
                    pendingMoves.removeAt(0)
                }, 300) // Delay of 300ms
            }

        })
        animator.duration = timeAnimateMs.toLong()
        animator.start()
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
}
