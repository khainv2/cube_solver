package com.khainv9.kubesolver.camera

import com.khainv9.kubesolver.cubeview.ColorfulCube
import com.khainv9.kubesolver.cubeview.CubeColor
import com.khainv9.kubesolver.cubeview.Direction
import com.khainv9.kubesolver.cubeview.Step

enum class Rotation {
    Deg_0, Deg_90, Deg_180, Deg_270
}

// Được sử dụng trong ứng dụng scan rubik, cho phép quét và đoán trạng thái hiện tại
class PhaseControl {

    var step = Step.Step_Top
    private val colors: Array<Array<CubeColor>>

    companion object {
        val GetDirectionByStep = arrayOf(
            Direction.LeftToRight,
            Direction.BottomToTop,
            Direction.BottomToTop,
            Direction.BottomToTop,
            Direction.LeftToRight,
            Direction.LeftToRight,
        )
    }

    init {
        colors = Array(6) { Array(9) { CubeColor.UNKNOWN } }
    }

    // Reset lại trạng thái quét
    fun reset() {
        step = Step.Step_Top
        for (i in 0 until 6) {
            for (j in 0 until 9) {
                colors[i][j] = CubeColor.UNKNOWN
            }
        }
    }

    fun getSuggestDirection(): Direction {
        return GetDirectionByStep[step.ordinal]
    }

    fun updateColor(colors: Array<CubeColor>, rot: Rotation) {
        for (i in 0 until 9) {
            when (rot) {
                Rotation.Deg_0 -> {
                    this.colors[step.ordinal][i] = colors[i]
                }
                Rotation.Deg_90 -> {
                    this.colors[step.ordinal][i] = colors[6 - (i % 3) * 3 + (i / 3)]
                }
                Rotation.Deg_180 -> {
                    this.colors[step.ordinal][i] = colors[8 - i]
                }
                Rotation.Deg_270 -> {
                    this.colors[step.ordinal][i] = colors[2 - (i / 3) + (i % 3) * 3]
                }
            }
        }
    }

    fun stepNext() {
        step = Step.values()[(step.ordinal + 1) % Step.values().size]
    }

    fun toColorfulCube(): ColorfulCube {
        val colorfulCube = ColorfulCube()
        for (i in 0 until 6) {
            for (j in 0 until 9) {
                colorfulCube.colors[i][j] = colors[i][j]
            }
        }
        return colorfulCube
    }
}
