package com.khainv9.kubesolver.camera;

import android.util.Log;

import com.khainv9.kubesolver.cubeview.CubeColor;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorDetector {

    // Tham số cấu hình
    private double thresholdPercent = 0.5; // 60%

    // Ngưỡng HSV cho từng màu (BGR chuyển sang HSV)
    // Lưu ý: Hue trong OpenCV có giá trị từ 0 đến 180.
    // Màu RED có 2 khoảng (vì red nằm ở đầu và cuối vòng tròn Hue)
    private Scalar redLower1 = new Scalar(0, 100, 100);
    private Scalar redUpper1 = new Scalar(5, 255, 255);
    private Scalar redLower2 = new Scalar(170, 100, 100);
    private Scalar redUpper2 = new Scalar(180, 255, 255);

    private Scalar yellowLower = new Scalar(20, 100, 100);
    private Scalar yellowUpper = new Scalar(35, 255, 255);

    private Scalar orangeLower = new Scalar(5, 100, 100);
    private Scalar orangeUpper = new Scalar(20, 255, 255);

    private Scalar greenLower = new Scalar(50, 100, 100);
    private Scalar greenUpper = new Scalar(70, 255, 255);

    private Scalar blueLower = new Scalar(100, 100, 100);
    private Scalar blueUpper = new Scalar(130, 255, 255);

    // Ngưỡng cho màu trắng: saturation thấp, value cao
    private Scalar whiteLower = new Scalar(0, 0, 180);
    private Scalar whiteUpper = new Scalar(180, 40, 255);

    public static void drawHueInfo(Mat frame, Mat hsvImage, Rect boundingRect) {
        // Cắt vùng HSV cần xử lý
        Mat hsvROI = new Mat(hsvImage, boundingRect);

        // Tách kênh Hue từ ảnh HSV
        List<Mat> hsvChannels = new ArrayList<>();
        Core.split(hsvROI, hsvChannels);
        Mat hueChannel = hsvChannels.get(0);

        // Tính histogram của kênh Hue
        Map<Integer, Integer> hueHistogram = new HashMap<>();
        int totalPixels = 0;

        for (int i = 0; i < hueChannel.rows(); i++) {
            for (int j = 0; j < hueChannel.cols(); j++) {
                int hueValue = (int) hueChannel.get(i, j)[0];
                hueHistogram.put(hueValue, hueHistogram.getOrDefault(hueValue, 0) + 1);
                totalPixels++;
            }
        }

        // Tìm giá trị Hue chiếm đa số (lớn hơn 60%)
        int dominantHue = -1;
        int maxCount = 0;
        int threshold = (int) (totalPixels * 0.6);

        for (Map.Entry<Integer, Integer> entry : hueHistogram.entrySet()) {
            if (entry.getValue() > maxCount && entry.getValue() > threshold) {
                dominantHue = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        Log.d("Info", "Dominant " + dominantHue);

        // Nếu tìm thấy màu chiếm đa số, vẽ lên frame
        if (dominantHue != -1) {
            Point textPosition = new Point(boundingRect.x, boundingRect.y - 10);
            Imgproc.putText(frame, "Hue: " + dominantHue, textPosition,
                    Imgproc.INTER_TAB_SIZE, 0.6,
                    new Scalar(255, 255, 255), 2);
        }
    }
    public List<CubeColor> processFrame(Mat frame, List<Quadrilateral> regions) {
        List<CubeColor> cubeColors = new ArrayList<>();

        Mat bgr = new Mat();
        Imgproc.cvtColor(frame, bgr, Imgproc.COLOR_RGBA2BGR);

        // Chuyển đổi frame sang HSV chỉ một lần
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(bgr, hsvImage, Imgproc.COLOR_BGR2HSV);
        int index = 0;

//        Log.d("Log", "Total regions " + regions.size());

        // Duyệt qua từng vùng (tứ giác) đã biết, giả sử Quadrilateral chứa 4 điểm
        for (Quadrilateral quad : regions) {
            // Tính bounding rectangle cho vùng này
            MatOfPoint matOfPoint = new MatOfPoint();
            matOfPoint.fromList(quad.getPoints());
            Rect boundingRect = Imgproc.boundingRect(matOfPoint);
            // Lấy submat HSV tương ứng với bounding rectangle
            Mat hsvROI = new Mat(hsvImage, boundingRect);
            // Lấy mask cho vùng này (có thể cũng được tính toán dựa trên bounding rectangle nếu quad đã được biến đổi)
            Mat fullMask = Mat.zeros(frame.size(), CvType.CV_8UC1);
            List<Point> pts = quad.getPoints();
            MatOfPoint contour = new MatOfPoint();
            contour.fromList(pts);
            List<MatOfPoint> contours = new ArrayList<>();
            contours.add(contour);
            Imgproc.fillPoly(fullMask, contours, new Scalar(255));
            // Cắt mask theo bounding rectangle
            Mat maskROI = new Mat(fullMask, boundingRect);

            // Gọi hàm detectColor tối ưu (phiên bản nhận sẵn hsvROI và mask)
            CubeColor color = detectColorOptimized(frame, hsvROI, maskROI, index);
            // Xử lý màu (ví dụ: vẽ kết quả hoặc lưu kết quả vào một mảng)
            cubeColors.add(color);
            // ...
//
//            if (index++ == 0) {
//                drawHueInfo(frame, hsvImage, boundingRect);
//            }

            // Giải phóng bộ nhớ của mask và fullMask nếu không cần dùng nữa
            fullMask.release();
            maskROI.release();
        }
        hsvImage.release();
        return cubeColors;
    }

    public CubeColor detectColorOptimized(Mat frame, Mat hsvROI, Mat mask, int idx) {
        int totalPixels = Core.countNonZero(mask);
        if (totalPixels == 0) {
            return CubeColor.UNKNOWN;
        }
        // --- Màu RED ---
        Mat redMask1 = new Mat();
        Mat redMask2 = new Mat();
        Core.inRange(hsvROI, redLower1, redUpper1, redMask1);
        Core.inRange(hsvROI, redLower2, redUpper2, redMask2);
        Mat redMask = new Mat();
        Core.add(redMask1, redMask2, redMask);
        int redCount = Core.countNonZero(redMask);



        redMask1.release();
        redMask2.release();
        redMask.release();

        // --- Màu YELLOW ---
        Mat yellowMask = new Mat();
        Core.inRange(hsvROI, yellowLower, yellowUpper, yellowMask);
        int yellowCount = Core.countNonZero(yellowMask);
        yellowMask.release();

        // --- Màu ORANGE ---
        Mat orangeMask = new Mat();
        Core.inRange(hsvROI, orangeLower, orangeUpper, orangeMask);
        int orangeCount = Core.countNonZero(orangeMask);
        orangeMask.release();

        // --- Màu GREEN ---
        Mat greenMask = new Mat();
        Core.inRange(hsvROI, greenLower, greenUpper, greenMask);
        int greenCount = Core.countNonZero(greenMask);
        greenMask.release();

        // --- Màu BLUE ---
        Mat blueMask = new Mat();
        Core.inRange(hsvROI, blueLower, blueUpper, blueMask);
        int blueCount = Core.countNonZero(blueMask);
        blueMask.release();

        // --- Màu WHITE ---
        Mat whiteMask = new Mat();
        Core.inRange(hsvROI, whiteLower, whiteUpper, whiteMask);
        int whiteCount = Core.countNonZero(whiteMask);
        whiteMask.release();

//        if (idx == 0) {
//            Imgproc.putText(frame, "Red: " + redCount + "  Yellow: " + yellowCount + "  Orange: " + orangeCount + "  Green: " + greenCount
//                            + "  Blue: " + blueCount + "  White: " + whiteCount + "  Total: " + totalPixels,
//                    new Point(20, 20), Imgproc.INTER_TAB_SIZE, 0.5, new Scalar(255, 255, 255), 2);
//        }
        if (((double) redCount / totalPixels) >= thresholdPercent) {
            return CubeColor.RED;
        }
        if (((double) yellowCount / totalPixels) >= thresholdPercent) {
            return CubeColor.YELLOW;
        }
        if (((double) orangeCount / totalPixels) >= thresholdPercent) {
            return CubeColor.ORANGE;
        }
        if (((double) greenCount / totalPixels) >= thresholdPercent) {
            return CubeColor.GREEN;
        }
        if (((double) blueCount / totalPixels) >= thresholdPercent) {
            return CubeColor.BLUE;
        }
        if (((double) whiteCount / totalPixels) >= thresholdPercent) {
            return CubeColor.WHITE;
        }

        return CubeColor.UNKNOWN;
    }

}
