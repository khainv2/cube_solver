package com.khainv9.kubesolver.cubeview

enum class Axis {
    X, Y, Z
}

enum class CubeColor {
    RED, BLUE, WHITE, GREEN, YELLOW, ORANGE, UNKNOWN
}

enum class Direction {
    NoDirection, LeftToRight, RightToLeft, TopToBottom, BottomToTop
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
    
    fun getInitColor(): CubeColor {
        return when (this) {
            FRONT -> CubeColor.WHITE
            LEFT -> CubeColor.BLUE
            RIGHT -> CubeColor.GREEN
            BACK -> CubeColor.YELLOW
            UP -> CubeColor.RED
            DOWN -> CubeColor.ORANGE
        }
    }

    companion object {
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
    }


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

    fun getFace(): Face {
        return when (this) {
            U, U_, U2 -> Face.UP
            D, D_, D2 -> Face.DOWN
            L, L_, L2 -> Face.LEFT
            R, R_, R2 -> Face.RIGHT
            F, F_, F2 -> Face.FRONT
            B, B_, B2 -> Face.BACK
        }
    }

    fun isClockwise(): Boolean {
        return when (this) {
            U, D, L, R, F, B -> true
            else -> false
        }
    }
    
    fun isAntiClockwise(): Boolean {
        return when (this) {
            U_, D_, L_, R_, F_, B_ -> true
            else -> false
        }
    }

    fun isDouble(): Boolean {
        return when (this) {
            U2, D2, L2, R2, F2, B2 -> true
            else -> false
        }
    }
}

enum class TilePos {
    TOP_LEFT, TOP_MID, TOP_RIGHT,
    MID_LEFT, MID_MID, MID_RIGHT,
    BOT_LEFT, BOT_MID, BOT_RIGHT
}
