package com.khainv9.kubesolver.cubeview


import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin


class Cube3D(private var cubeState: ColorfulCube) {
    var move: Move? = null
    var angle: Float = 0.0f

    fun getCubeState(): ColorfulCube {
        return cubeState
    }

    fun updateCube(cubeState: ColorfulCube) {
        this.cubeState = cubeState
    }

    // Mã shader vẫn giữ nguyên
    private val vertexShaderCode = """
         uniform mat4 uMVPMatrix;
         attribute vec4 aPosition;
         void main() {
             gl_Position = uMVPMatrix * aPosition;
         }
    """

    private val fragmentShaderCode = """
         precision mediump float;
         uniform vec4 uColor;
         void main() {
             gl_FragColor = uColor;
         }
    """

    private var program: Int = 0

    init {
        // Biên dịch shader và liên kết chương trình
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    /**
     * Hàm chuyển CubeColor thành mảng RGBA.
     */
    private fun getColor(color: CubeColor): FloatArray = when(color) {
        CubeColor.RED -> floatArrayOf(1f, 0f, 0f, 1f)
        CubeColor.BLUE -> floatArrayOf(0f, 0f, 1f, 1f)
        CubeColor.WHITE -> floatArrayOf(1f, 1f, 1f, 1f)
        CubeColor.GREEN -> floatArrayOf(0f, 1f, 0f, 1f)
        CubeColor.YELLOW -> floatArrayOf(1f, 1f, 0f, 1f)
        CubeColor.ORANGE -> floatArrayOf(1f, 0.5f, 0f, 1f)
        CubeColor.UNKNOWN -> floatArrayOf(0.5f, 0.5f, 0.5f, 0.5f)
    }

    private fun rotateArray(tileVertices: FloatArray, axis: Axis, angle: Float): FloatArray {
        val radians = Math.toRadians(angle.toDouble())
        val cos = cos(radians).toFloat()
        val sin = sin(radians).toFloat()

        for (i in tileVertices.indices step 3) {
            val x = tileVertices[i]
            val y = tileVertices[i + 1]
            val z = tileVertices[i + 2]
            when (axis) {
                Axis.X -> { // X axis
                    tileVertices[i] = x
                    tileVertices[i + 1] = cos * y - sin * z
                    tileVertices[i + 2] = sin * y + cos * z
                }
                Axis.Y -> { // Y axis
                    tileVertices[i] = cos * x + sin * z
                    tileVertices[i + 1] = y
                    tileVertices[i + 2] = -sin * x + cos * z
                }
                Axis.Z -> { // Z axis
                    tileVertices[i] = cos * x - sin * y
                    tileVertices[i + 1] = sin * x + cos * y
                    tileVertices[i + 2] = z
                }
            }
        }
        return tileVertices
    }

    /**
     * Tính toán tọa độ 4 đỉnh của một ô (tile) trên một mặt của rubik.
     * Các mặt được đánh số theo thứ tự:
     *   0: Top   (y = 1)
     *   1: Left  (x = -1)
     *   2: Front (z = 1)
     *   3: Right (x = 1)
     *   4: Back  (z = -1)
     *   5: Bottom(y = -1)
     *
     * Mỗi mặt có diện tích từ -1 đến 1 theo các trục phù hợp. Tile được chia đều thành 3 hàng và 3 cột.
     */
    private fun getTileVertices(face: Face, row: Int, col: Int): FloatArray {
        val tileSize = 2f / 3f  // kích thước của mỗi ô trên mặt (với mặt có độ dài 2)
        val start = -1f       // giá trị bắt đầu của tọa độ (cạnh trái hoặc trên)
        return when (face) {
            Face.UP -> { // Top face
                val x0 = start + col * tileSize
                val x1 = start + (col + 1) * tileSize
                val z0 = -1f + row * tileSize
                val z1 = -1f + (row + 1) * tileSize
                floatArrayOf(
                    x0, 1f, z0,
                    x0, 1f, z1,
                    x1, 1f, z1,
                    x1, 1f, z0
                )
            }
            Face.LEFT -> { // Left face
                val z0 = -1f + col * tileSize
                val z1 = -1f + (col + 1) * tileSize
                val y0 = 1f - row * tileSize
                val y1 = 1f - (row + 1) * tileSize
                floatArrayOf(
                    -1f, y0, z0,
                    -1f, y1, z0,
                    -1f, y1, z1,
                    -1f, y0, z1
                )
            }
            Face.FRONT -> { // Front face
                val x0 = start + col * tileSize
                val x1 = start + (col + 1) * tileSize
                val y0 = 1f - row * tileSize
                val y1 = 1f - (row + 1) * tileSize
                // Sắp xếp theo thứ tự sao cho khi vẽ bằng TRIANGLE_FAN vẫn đảm bảo thứ tự vòng cung
                floatArrayOf(
                    x0, y0, 1f,
                    x0, y1, 1f,
                    x1, y1, 1f,
                    x1, y0, 1f
                )
            }
            Face.RIGHT -> { // Right face
                val z0 = 1f - col * tileSize
                val z1 = 1f - (col + 1) * tileSize
                val y0 = 1f - row * tileSize
                val y1 = 1f - (row + 1) * tileSize
                floatArrayOf(
                    1f, y0, z0,
                    1f, y1, z0,
                    1f, y1, z1,
                    1f, y0, z1
                )
            }
            Face.BACK -> { // Back face
                // Đảo ngược trục X để đảm bảo thứ tự đỉnh phù hợp (winding order)
                val x0 = 1f - col * tileSize
                val x1 = 1f - (col + 1) * tileSize
                val y0 = 1f - row * tileSize
                val y1 = 1f - (row + 1) * tileSize
                floatArrayOf(
                    x0, y0, -1f,
                    x0, y1, -1f,
                    x1, y1, -1f,
                    x1, y0, -1f
                )
            }
            Face.DOWN -> { // Bottom face
                val x0 = start + col * tileSize
                val x1 = start + (col + 1) * tileSize
                // Ở mặt dưới, ta duyệt theo trục Z từ 1 đến -1
                val z0 = 1f - row * tileSize
                val z1 = 1f - (row + 1) * tileSize
                floatArrayOf(
                    x0, -1f, z0,
                    x0, -1f, z1,
                    x1, -1f, z1,
                    x1, -1f, z0
                )
            }
            else -> floatArrayOf()
        }
    }



    private fun rotate(face: Face, row: Int, col: Int, angle: Float, tileVertices: FloatArray): FloatArray {
        val m = move
        if (m == null)
            return tileVertices
        if (m.getFace() == face || Face.relatedFace(m.getFace(), face, row, col)) {
            return rotateArray(tileVertices, m.getFace().getAxis(), angle)
        }
        return tileVertices
    }

    /**
     * Vẽ toàn bộ rubik với lưới 3x3 trên mỗi mặt.
     */
    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)
        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val colorHandle = GLES20.glGetUniformLocation(program, "uColor")

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glEnableVertexAttribArray(positionHandle)

        for (face in 0 until 6) {
            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    val tileIndex = row * 3 + col
                    var tileVertices = getTileVertices(Face.entries[face], row, col)


                    tileVertices = rotate(Face.entries[face], row, col, angle, tileVertices)

                    val tileBuffer: FloatBuffer = ByteBuffer.allocateDirect(tileVertices.size * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer().apply {
                            put(tileVertices)
                            position(0)
                        }

                    // Thiết lập vị trí đỉnh
                    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, tileBuffer)

                    // --- VẼ VIỀN MÀU ĐEN ---
                    GLES20.glUniform4fv(colorHandle, 1, floatArrayOf(0f, 0f, 0f, 1f), 0)
                    GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4)

                    // --- VẼ Ô VỚI MÀU CỦA RUBIK ---
                    val cubeColor = cubeState.colors[face][tileIndex]
                    val rgba = getColor(cubeColor)
                    GLES20.glUniform4fv(colorHandle, 1, rgba, 0)
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

                }
            }
        }

        GLES20.glDisableVertexAttribArray(positionHandle)
    }


    /**
     * Hàm tải shader từ mã nguồn.
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
