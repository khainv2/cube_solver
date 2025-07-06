package com.khainv9.kubesolver.cubeview

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.khainv9.kubesolver.cubeview.Direction

class RubiksCubeGLSurfaceView(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {

    val renderer: RubiksCubeRenderer

    init {
        // Sử dụng OpenGL ES 2.0
        setEGLContextClientVersion(2)
        renderer = RubiksCubeRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun updateCube(cubeState: ColorfulCube) {
        renderer.updateCube(cubeState)
    }

    fun resetRotation() {
        renderer.resetRotation()
    }

    fun rotate(direction: Direction) {
        renderer.rotate(direction, 500)
    }

    // Xử lý cảm ứng để xoay khối rubik
    private var previousX = 0f
    private var previousY = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val dx = x - previousX
                    val dy = y - previousY
                    renderer.angleX += dy * TOUCH_SCALE_FACTOR
                    renderer.angleY += dx * TOUCH_SCALE_FACTOR
                    renderer.targetAngleX = renderer.angleX
                    renderer.targetAngleY = renderer.angleY

                    Log.d("CubeView", "angleX: ${renderer.angleX}, angleY: ${renderer.angleY}")
                }
            }
            previousX = x
            previousY = y
        }
        return true
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR = 0.5f
    }
}
