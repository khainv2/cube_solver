package com.khainv9.kubesolver.cubeview

import android.annotation.SuppressLint
import androidx.camera.core.Logger

/// See comment in C++ code
enum class CubeColor {
    RED, BLUE, WHITE, GREEN, YELLOW, ORANGE, UNKNOWN
}

enum class Axis {
    X, Y, Z
}

enum class Face {
    FRONT, BACK, LEFT, RIGHT, UP, DOWN;

    fun getAxis(): Axis {
        return when (this) {
            FRONT, BACK -> Axis.Z
            LEFT, RIGHT -> Axis.X
            UP, DOWN -> Axis.Y
        }
    }
    fun getInitColor() : CubeColor {
        return when (this) {
            FRONT -> CubeColor.WHITE
            LEFT -> CubeColor.BLUE
            RIGHT -> CubeColor.GREEN
            BACK -> CubeColor.YELLOW
            UP -> CubeColor.RED
            DOWN -> CubeColor.ORANGE
        }
    }
}

//enum class

enum class TilePos {
    TOP_LEFT, TOP_MID, TOP_RIGHT,
    MID_LEFT, MID_MID, MID_RIGHT,
    BOT_LEFT, BOT_MID, BOT_RIGHT
}

enum class Move {
    U, U_, U2, D, D_, D2,
    L, L_, L2, R, R_, R2,
    F, F_, F2, B, B_, B2;

    companion object {

        fun parseMove(move: String): Move {
            return when (move) {
                "U" -> U
                "U'" -> U_
                "U2" -> U2
                "D" -> D
                "D'" -> D_
                "D2" -> D2
                "L" -> L
                "L'" -> L_
                "L2" -> L2
                "R" -> R
                "R'" -> R_
                "R2" -> R2
                "F" -> F
                "F'" -> F_
                "F2" -> F2
                "B" -> B
                "B'" -> B_
                "B2" -> B2
                else -> throw IllegalArgumentException("Invalid move: $move")
            }
        }

        fun parseMoveList(moveList: String): List<Move> {
            return moveList.split(" ").map { parseMove(it) }
        }
    }


    fun getFace() : Face {
        return when (this) {
            U, U_, U2 -> Face.UP
            D, D_, D2 -> Face.DOWN
            L, L_, L2 -> Face.LEFT
            R, R_, R2 -> Face.RIGHT
            F, F_, F2 -> Face.FRONT
            B, B_, B2 -> Face.BACK
        }
    }

    fun isClockwise() : Boolean {
        return when (this) {
            U, D, L, R, F, B -> true
            else -> false
        }
    }
    fun isAntiClockwise() : Boolean {
        return when (this) {
            U_, D_, L_, R_, F_, B_ -> true
            else -> false
        }
    }

    fun isDouble() : Boolean {
        return when (this) {
            U2, D2, L2, R2, F2, B2 -> true
            else -> false
        }
    }

}
class ColorfulCube {
    // Giả sử thứ tự các mặt: FRONT, BACK, LEFT, RIGHT, TOP, BOTTOM
    var colors: Array<Array<CubeColor>> = Array(6) { Array(9) { CubeColor.RED } }

    fun rotateArray(arr: Array<CubeColor>, clockwise: Boolean): Array<CubeColor> {
        return if (clockwise) {
            arrayOf(arr[6], arr[3], arr[0], arr[7], arr[4], arr[1], arr[8], arr[5], arr[2])
        } else {
            arrayOf(arr[2], arr[5], arr[8], arr[1], arr[4], arr[7], arr[0], arr[3], arr[6])
        }
    }
    fun rotateDouble(arr: Array<CubeColor>): Array<CubeColor> {
        return rotateArray(rotateArray(arr, true), true)
    }
    companion object {
        val frontList = arrayOf(Face.UP, Face.RIGHT, Face.DOWN, Face.LEFT)
        val upList = arrayOf(Face.FRONT, Face.LEFT, Face.BACK, Face.RIGHT)
        val leftList = arrayOf(Face.UP, Face.FRONT, Face.DOWN, Face.BACK)
        val rightList = arrayOf(Face.UP, Face.BACK, Face.DOWN, Face.FRONT)
        val backList = arrayOf(Face.UP, Face.LEFT, Face.DOWN, Face.RIGHT)
        val downList = arrayOf(Face.FRONT, Face.RIGHT, Face.BACK, Face.LEFT)
        val totalListByFace = arrayOf(frontList, backList, leftList, rightList, upList, downList)

        val frontTileList = arrayOf(6, 7, 8, 0, 3, 6, 2, 1, 0, 8, 5, 2)
        val upTileList    = arrayOf(2, 1, 0, 2, 1, 0, 2, 1, 0, 2, 1, 0)
        val leftTileList  = arrayOf(0, 3, 6, 0, 3, 6, 0, 3, 6, 8, 5, 2)
        val rightTileList = arrayOf(8, 5, 2, 0, 3, 6, 8, 5, 2, 8, 5, 2)
        val backTileList  = arrayOf(2, 1, 0, 0, 3, 6, 6, 7, 8, 8, 5, 2)
        val downTileList  = arrayOf(6, 7, 8, 6, 7, 8, 6, 7, 8, 6, 7, 8)
        val totalListTileByFace = arrayOf(frontTileList, backTileList, leftTileList,
            rightTileList, upTileList, downTileList)
    }
    
    fun updateByMove(move: Move) {
        // Create copy of current colors
        val newColors = Array(6) { Array(9) { CubeColor.UNKNOWN } }
        for (i in 0 until 6) {
            for (j in 0 until 9) {
                newColors[i][j] = colors[i][j]
            }
        }

        val face = move.getFace()
        val clockwise = move.isClockwise()
        val antiClockwise = move.isAntiClockwise()
        val double = move.isDouble()
        if (clockwise) {
            newColors[face.ordinal] = rotateArray(colors[face.ordinal], true)
        } else if (antiClockwise) {
            newColors[face.ordinal] = rotateArray(colors[face.ordinal], false)
        } else if (double) {
            newColors[face.ordinal] = rotateDouble(colors[face.ordinal])
        }

        val affectedFace = totalListByFace[face.ordinal]
        val affectedTilePos = totalListTileByFace[face.ordinal]
        var faceInc = 0
        if (move.isClockwise()) faceInc = 1
        else if (move.isDouble()) faceInc = 2
        else if (move.isAntiClockwise()) faceInc = 3
        val tileInc = faceInc * 3
        for (i in 0 until 4) {
            for (j in 0 until 3) {
                val currFace = affectedFace[i]
                val currTilePos = affectedTilePos[i * 3 + j]
                val nextFace = affectedFace[(i + faceInc) % 4]
                val nextTilePos = affectedTilePos[(i * 3 + j + tileInc) % 12]
                newColors[nextFace.ordinal][nextTilePos] = colors[currFace.ordinal][currTilePos]
            }
        }

        // Copy new array to array
        for (i in 0 until 6) {
            for (j in 0 until 9) {
                colors[i][j] = newColors[i][j]
            }
        }
    }
}


fun relatedFace(movedFace: Face, checkedFace: Face, row: Int, col: Int): Boolean {
    when (movedFace) {
        Face.LEFT -> {
            if (checkedFace == Face.FRONT && col == 0) {
                return true
            }
            if (checkedFace == Face.BACK && col == 2) {
                return true
            }
            if (checkedFace == Face.UP && col == 0) {
                return true
            }
            if (checkedFace == Face.DOWN && col == 0) {
                return true
            }
        }
        Face.RIGHT -> {
            if (checkedFace == Face.FRONT && col == 2) {
                return true
            }
            if (checkedFace == Face.BACK && col == 0) {
                return true
            }
            if (checkedFace == Face.UP && col == 2) {
                return true
            }
            if (checkedFace == Face.DOWN && col == 2) {
                return true
            }
        }
        Face.FRONT -> {
            if (checkedFace == Face.LEFT && col == 2) {
                return true
            }
            if (checkedFace == Face.RIGHT && col == 0) {
                return true
            }
            if (checkedFace == Face.UP && row == 2) {
                return true
            }
            if (checkedFace == Face.DOWN && row == 0) {
                return true
            }
        }
        Face.BACK -> {
            if (checkedFace == Face.LEFT && col == 0) {
                return true
            }
            if (checkedFace == Face.RIGHT && col == 2) {
                return true
            }
            if (checkedFace == Face.UP && row == 0) {
                return true
            }
            if (checkedFace == Face.DOWN && row == 2) {
                return true
            }
        }
        Face.DOWN -> {
            if (checkedFace == Face.LEFT && row == 2) {
                return true
            }
            if (checkedFace == Face.RIGHT && row == 2) {
                return true
            }
            if (checkedFace == Face.FRONT && row == 2) {
                return true
            }
            if (checkedFace == Face.BACK && row == 2) {
                return true
            }
        }
        Face.UP -> {
            if (checkedFace == Face.LEFT && row == 0) {
                return true
            }
            if (checkedFace == Face.RIGHT && row == 0) {
                return true
            }
            if (checkedFace == Face.FRONT && row == 0) {
                return true
            }
            if (checkedFace == Face.BACK && row == 0) {
                return true
            }

        }
    }
    return false;
}
