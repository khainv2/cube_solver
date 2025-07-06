package com.khainv9.kubesolver.cubeview

import com.khainv9.kubesolver.cubeview.CubeColor
import com.khainv9.kubesolver.cubeview.Face
import com.khainv9.kubesolver.cubeview.Move

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

