package com.khainv9.kubesolver.camera

import android.util.Log
import com.khainv9.kubesolver.cubeview.CubeColor
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class ColorDetector {

    // Tham số cấu hình
    private val thresholdPercent = 0.5 // 60%

    // Ngưỡng HSV cho từng màu (BGR chuyển sang HSV)
    // Lưu ý: Hue trong OpenCV có giá trị từ 0 đến 180.
    // Màu RED có 2 khoảng (vì red nằm ở đầu và cuối vòng tròn Hue)
    private val redLower1 = Scalar(0.0, 100.0, 100.0)
    private val redUpper1 = Scalar(5.0, 255.0, 255.0)
    private val redLower2 = Scalar(170.0, 100.0, 100.0)
    private val redUpper2 = Scalar(180.0, 255.0, 255.0)

    private val yellowLower = Scalar(20.0, 100.0, 100.0)
    private val yellowUpper = Scalar(35.0, 255.0, 255.0)

    private val orangeLower = Scalar(5.0, 100.0, 100.0)
    private val orangeUpper = Scalar(20.0, 255.0, 255.0)

    private val greenLower = Scalar(50.0, 100.0, 100.0)
    private val greenUpper = Scalar(70.0, 255.0, 255.0)

    private val blueLower = Scalar(100.0, 100.0, 100.0)
    private val blueUpper = Scalar(130.0, 255.0, 255.0)

    // Ngưỡng cho màu trắng: saturation thấp, value cao
    private val whiteLower = Scalar(0.0, 0.0, 180.0)
    private val whiteUpper = Scalar(180.0, 40.0, 255.0)

    companion object {
        fun drawHueInfo(frame: Mat, hsvImage: Mat, boundingRect: Rect) {
            // Cắt vùng HSV cần xử lý
            val hsvROI = Mat(hsvImage, boundingRect)

            // Tách kênh Hue từ ảnh HSV
            val hsvChannels = mutableListOf<Mat>()
            Core.split(hsvROI, hsvChannels)
            val hueChannel = hsvChannels[0]

            // Tính histogram của kênh Hue
            val hueHistogram = mutableMapOf<Int, Int>()
            var totalPixels = 0

            for (i in 0 until hueChannel.rows()) {
                for (j in 0 until hueChannel.cols()) {
                    val hueValue = hueChannel.get(i, j)[0].toInt()
                    hueHistogram[hueValue] = hueHistogram.getOrDefault(hueValue, 0) + 1
                    totalPixels++
                }
            }

            // Tìm giá trị Hue chiếm đa số (lớn hơn 60%)
            var dominantHue = -1
            var maxCount = 0
            val threshold = (totalPixels * 0.6).toInt()

            for ((hue, count) in hueHistogram) {
                if (count > maxCount && count > threshold) {
                    dominantHue = hue
                    maxCount = count
                }
            }

            Log.d("Info", "Dominant $dominantHue")

            // Nếu tìm thấy màu chiếm đa số, vẽ lên frame
            if (dominantHue != -1) {
                val textPosition = Point(boundingRect.x.toDouble(), boundingRect.y - 10.0)
                Imgproc.putText(
                    frame, "Hue: $dominantHue", textPosition,
                    Imgproc.INTER_TAB_SIZE, 0.6,
                    Scalar(255.0, 255.0, 255.0), 2
                )
            }
        }
    }

    fun processFrame(frame: Mat, regions: List<Quadrilateral>): List<CubeColor> {
        val cubeColors = mutableListOf<CubeColor>()

        val bgr = Mat()
        Imgproc.cvtColor(frame, bgr, Imgproc.COLOR_RGBA2BGR)

        // Chuyển đổi frame sang HSV chỉ một lần
        val hsvImage = Mat()
        Imgproc.cvtColor(bgr, hsvImage, Imgproc.COLOR_BGR2HSV)
        var index = 0

        // Log.d("Log", "Total regions ${regions.size}")

        // Duyệt qua từng vùng (tứ giác) đã biết, giả sử Quadrilateral chứa 4 điểm
        for (quad in regions) {
            // Tính bounding rectangle cho vùng này
            val matOfPoint = MatOfPoint()
            matOfPoint.fromList(quad.getPoints())
            val boundingRect = Imgproc.boundingRect(matOfPoint)
            
            // Lấy submat HSV tương ứng với bounding rectangle
            val hsvROI = Mat(hsvImage, boundingRect)
            
            // Lấy mask cho vùng này (có thể cũng được tính toán dựa trên bounding rectangle nếu quad đã được biến đổi)
            val fullMask = Mat.zeros(frame.size(), CvType.CV_8UC1)
            val pts = quad.getPoints()
            val contour = MatOfPoint()
            contour.fromList(pts)
            val contours = listOf(contour)
            Imgproc.fillPoly(fullMask, contours, Scalar(255.0))
            
            // Cắt mask theo bounding rectangle
            val maskROI = Mat(fullMask, boundingRect)

            // Gọi hàm detectColor tối ưu (phiên bản nhận sẵn hsvROI và mask)
            val color = detectColorOptimized(frame, hsvROI, maskROI, index)
            
            // Xử lý màu (ví dụ: vẽ kết quả hoặc lưu kết quả vào một mảng)
            cubeColors.add(color)

            // if (index++ == 0) {
            //     drawHueInfo(frame, hsvImage, boundingRect)
            // }

            // Giải phóng bộ nhớ của mask và fullMask nếu không cần dùng nữa
            fullMask.release()
            maskROI.release()
        }
        hsvImage.release()
        return cubeColors
    }

    fun detectColorOptimized(frame: Mat, hsvROI: Mat, mask: Mat, idx: Int): CubeColor {
        val totalPixels = Core.countNonZero(mask)
        if (totalPixels == 0) {
            return CubeColor.UNKNOWN
        }
        
        // --- Màu RED ---
        val redMask1 = Mat()
        val redMask2 = Mat()
        Core.inRange(hsvROI, redLower1, redUpper1, redMask1)
        Core.inRange(hsvROI, redLower2, redUpper2, redMask2)
        val redMask = Mat()
        Core.add(redMask1, redMask2, redMask)
        val redCount = Core.countNonZero(redMask)

        redMask1.release()
        redMask2.release()
        redMask.release()

        // --- Màu YELLOW ---
        val yellowMask = Mat()
        Core.inRange(hsvROI, yellowLower, yellowUpper, yellowMask)
        val yellowCount = Core.countNonZero(yellowMask)
        yellowMask.release()

        // --- Màu ORANGE ---
        val orangeMask = Mat()
        Core.inRange(hsvROI, orangeLower, orangeUpper, orangeMask)
        val orangeCount = Core.countNonZero(orangeMask)
        orangeMask.release()

        // --- Màu GREEN ---
        val greenMask = Mat()
        Core.inRange(hsvROI, greenLower, greenUpper, greenMask)
        val greenCount = Core.countNonZero(greenMask)
        greenMask.release()

        // --- Màu BLUE ---
        val blueMask = Mat()
        Core.inRange(hsvROI, blueLower, blueUpper, blueMask)
        val blueCount = Core.countNonZero(blueMask)
        blueMask.release()

        // --- Màu WHITE ---
        val whiteMask = Mat()
        Core.inRange(hsvROI, whiteLower, whiteUpper, whiteMask)
        val whiteCount = Core.countNonZero(whiteMask)
        whiteMask.release()

        // if (idx == 0) {
        //     Imgproc.putText(frame, "Red: $redCount  Yellow: $yellowCount  Orange: $orangeCount  Green: $greenCount  Blue: $blueCount  White: $whiteCount  Total: $totalPixels",
        //             Point(20.0, 20.0), Imgproc.INTER_TAB_SIZE, 0.5, Scalar(255.0, 255.0, 255.0), 2)
        // }
        
        if ((redCount.toDouble() / totalPixels) >= thresholdPercent) {
            return CubeColor.RED
        }
        if ((yellowCount.toDouble() / totalPixels) >= thresholdPercent) {
            return CubeColor.YELLOW
        }
        if ((orangeCount.toDouble() / totalPixels) >= thresholdPercent) {
            return CubeColor.ORANGE
        }
        if ((greenCount.toDouble() / totalPixels) >= thresholdPercent) {
            return CubeColor.GREEN
        }
        if ((blueCount.toDouble() / totalPixels) >= thresholdPercent) {
            return CubeColor.BLUE
        }
        if ((whiteCount.toDouble() / totalPixels) >= thresholdPercent) {
            return CubeColor.WHITE
        }

        return CubeColor.UNKNOWN
    }
}
