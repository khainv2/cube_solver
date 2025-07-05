package com.khainv9.kubesolver.camera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.khainv9.kubesolver.R;
import com.khainv9.kubesolver.cubeview.CubeColor;
import com.khainv9.kubesolver.cubeview.Direction;
import com.khainv9.kubesolver.cubeview.Face;
import com.khainv9.kubesolver.cubeview.Move;
import com.khainv9.kubesolver.cubeview.RubiksCubeGLSurfaceView;
import com.khainv9.kubesolver.cubeview.Step;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "CameraActivity";

    boolean readyToNext = false;

    CubeColor[] colors;

    PhaseControl phaseControl;

    private CameraBridgeViewBase cameraView;

    RubiksCubeGLSurfaceView cubeView;

    Step step = Step.Step_Top;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                cameraView.enableView()  ;
                cameraView.setOnTouchListener(CameraActivity.this);
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        phaseControl = new PhaseControl();

        cameraView = findViewById(R.id.camera_view);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);

        Button bt_Reset = findViewById(R.id.bt_Reset);
        Button bt_Next = findViewById(R.id.bt_Next);
        Button bt_Capture = findViewById(R.id.bt_Capture);
        Button bt_Test = findViewById(R.id.bt_Test);
        Button bt_Test2 = findViewById(R.id.bt_Test2);
        Button bt_Test3 = findViewById(R.id.bt_Test3);
        Button bt_Test4 = findViewById(R.id.bt_Test4);
        Button bt_Test5 = findViewById(R.id.bt_Test5);
        Button bt_Test6 = findViewById(R.id.bt_Test6);
        TextView tv_Instruction = findViewById(R.id.tv_Instruction);
        phaseControl.reset();

//        tv_Instruction.setText("Please capture the top face of the cube");
        bt_Capture.setOnClickListener(v -> {
            if (!isAllKnown()) {
                Toast.makeText(this, "Please capture all colors", Toast.LENGTH_SHORT).show();
                return;
            }
            readyToNext = true;
            phaseControl.updateColor(colors, Rotation.Deg_0);
            cubeView.updateCube(phaseControl.toColorfulCube());
        });

        bt_Reset.setOnClickListener(v -> {
            readyToNext = false;
            phaseControl.reset();
            cubeView.resetRotation();
//            cubeView.updateCube(phaseControl.toColorfulCube());
        });

        bt_Next.setOnClickListener(v -> {
            readyToNext = false;
            cubeView.rotate(phaseControl.getSuggestDirection());
            phaseControl.stepNext();
        });




        bt_Test.setOnClickListener(v -> {
            var str = "U' F2 L2 B2 F2 U' F2 R2 U' L2 D2 L2 F' D' R' F' R B' U' B2 L'";
            var moves = Move.Companion.parseMoveList(str);
            for (int i = 0; i < moves.size(); i++) {
                var move = moves.get(i);
                cubeView.getRenderer().startDoMove(move, 300);
            }
        });

        bt_Test2.setOnClickListener(v -> {
            cubeView.getRenderer().rotateByFace(Face.LEFT);
        });
        bt_Test3.setOnClickListener(v -> {
            cubeView.getRenderer().rotateByFace(Face.BACK);
        });

        bt_Test4.setOnClickListener(v -> {
            cubeView.getRenderer().rotateByFace(Face.RIGHT);
        });
        bt_Test5.setOnClickListener(v -> {
            cubeView.getRenderer().rotateByFace(Face.UP);
        });
        bt_Test6.setOnClickListener(v -> {
            cubeView.getRenderer().rotateByFace(Face.DOWN);
        });

        cubeView = findViewById(R.id.cube_view);

        colors = new CubeColor[9];
        for (int i = 0; i < 9; i++) {
            colors[i] = CubeColor.UNKNOWN;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();

        int width = frame.width();
        int height = frame.height();
        int size = Math.min(width, height);
        int cubePreferSize = size * 4 / 6;

        // Calculate hexagon points
        int paddingLeft = (width - size) / 2;
        if (paddingLeft < 0) paddingLeft = 0;

        Point[] faceVertex = new Point[4];
        double centerX = paddingLeft + (cubePreferSize / 2);
        double centerY = height / 2.0;
        Point center = new Point(centerX, centerY);

        for (int i = 0; i < 4; i++) {
            double angle = Math.toRadians(45 - i * 90); // Adjusted starting angle
            faceVertex[i] = new Point(centerX + cubePreferSize * Math.cos(angle), centerY - cubePreferSize * Math.sin(angle)); // Adjusted y-coordinate
        }


        List<Quadrilateral> quadrilaterals = getAllQuad(faceVertex[0], faceVertex[1], faceVertex[2], faceVertex[3]);
        List<CubeColor> cubeColors = detector.processFrame(frame, quadrilaterals);
        for (int i = 0; i < quadrilaterals.size(); i++) {
            Quadrilateral quad = quadrilaterals.get(i);
            colors[quad.hIndex * 3 + quad.vIndex] = cubeColors.get(i);
        }



        // Draw hexagon
        Scalar borderColor = new Scalar(0, 255, 0);
        Scalar vertexColor = new Scalar(0, 0, 255);
        Scalar gridColor = new Scalar(60, 60, 60);
        for (int i = 0; i < 4; i++) {
            Imgproc.line(frame, faceVertex[i], faceVertex[(i + 1) % 4], borderColor, 3);
        }

        // Vẽ lần lượt các đường nối bên trong
        drawGrid(frame, faceVertex[0], faceVertex[1], faceVertex[2], faceVertex[3]);

        drawSmallCorner(frame, faceVertex[0], faceVertex[1], faceVertex[2], faceVertex[3]);

        if (readyToNext) {
            drawArrowDirectionOnMat(frame, center, phaseControl.getSuggestDirection());
        }

        return frame;
    }

    boolean isAllKnown() {
        for (CubeColor color : colors) {
            if (color == CubeColor.UNKNOWN) {
                return false;
            }
        }
        return true;
    }

    void drawArrowDirectionOnMat(Mat frame, Point center, Direction direction) {
        int width = frame.cols();
        int height = frame.rows();
        double side = Math.min(width, height);
        double arrowLength = 0.6 * side;
        int arrowTipSize = 15; // 80 dp
        Scalar arrowColor = new Scalar(33, 255, 123);

        Point start, end;
        switch (direction) {
            case LeftToRight:
                start = new Point((width - arrowLength) / 2, height / 2);
                end = new Point(start.x + arrowLength, start.y);
                break;
            case RightToLeft:
                start = new Point((width + arrowLength) / 2, height / 2);
                end = new Point(start.x - arrowLength, start.y);
                break;
            case TopToBottom:
                start = new Point(width / 2, (height - arrowLength) / 2);
                end = new Point(start.x, start.y + arrowLength);
                break;
            case BottomToTop:
                start = new Point(width / 2, (height + arrowLength) / 2);
                end = new Point(start.x, start.y - arrowLength);
                break;
            default:
                return;
        }

        start.x += center.x - width / 2;
        end.x += center.x - width / 2;
        Imgproc.arrowedLine(frame, start, end, arrowColor, 30, Imgproc.LINE_4, 0, 0.5);
    }
    private Scalar getColorByCubeColor(CubeColor color) {
        switch (color) {
            case RED: return new Scalar(255, 0, 0);
            case BLUE: return new Scalar(0, 0, 255);
            case WHITE: return new Scalar(255, 255, 255);
            case GREEN: return new Scalar(0, 255, 0);
            case YELLOW: return new Scalar(255, 255, 0);
            case ORANGE: return new Scalar(255, 165, 0);
            default: return new Scalar(128, 128, 128);
        }
    }


    public void drawGrid(Mat frame, Point p1, Point p2, Point p3, Point p4) {
        int numGrid = 3;
        // Calculate step sizes for horizontal and vertical lines
        double horizontalStepX = (p2.x - p1.x) / numGrid;
        double horizontalStepY = (p2.y - p1.y) / numGrid;
        double verticalStepX = (p4.x - p1.x) / numGrid;
        double verticalStepY = (p4.y - p1.y) / numGrid;

        Scalar gridColor = new Scalar(255, 255, 255);
        int gridWidth = 1;
        // Draw horizontal grid lines
        for (int i = 1; i < numGrid; i++) {
            Point start = new Point(p1.x + verticalStepX * i, p1.y + verticalStepY * i);
            Point end = new Point(start.x + horizontalStepX * numGrid, start.y + horizontalStepY * numGrid);
            Imgproc.line(frame, start, end, gridColor, gridWidth);
        }

        // Draw vertical grid lines
        for (int i = 1; i < numGrid; i++) {
            Point start = new Point(p1.x + horizontalStepX * i, p1.y + horizontalStepY * i);
            Point end = new Point(start.x + verticalStepX * numGrid, start.y + verticalStepY * numGrid);
            Imgproc.line(frame, start, end, gridColor, gridWidth);
        }

//        int channels = frame.channels();
//        Log.d("MyLog", "Channel: " +  CvType.typeToString(frame.type()));
    }
    public Mat drawSmallCorner(Mat frame, Point p1, Point p2, Point p3, Point p4) {
        int numGrid = 3;

        // Calculate step sizes for horizontal and vertical lines
        double horizontalStepX = (p2.x - p1.x) / numGrid;
        double horizontalStepY = (p2.y - p1.y) / numGrid;
        double verticalStepX = (p4.x - p1.x) / numGrid;
        double verticalStepY = (p4.y - p1.y) / numGrid;

        double factor = 0.2;
        for (int hIndex = 0; hIndex < numGrid; hIndex++) {
            // Draw vertical grid lines
            for (int vIndex = 0; vIndex < numGrid; vIndex++) {
                CubeColor color = colors[hIndex * 3 + vIndex];
                Point pp = new Point(p1.x + verticalStepX * hIndex + horizontalStepX * vIndex,
                        p1.y + verticalStepY * hIndex + horizontalStepY * vIndex);
                Point ppx = new Point(pp.x + verticalStepX * factor,
                        pp.y + verticalStepY * factor);
                Point ppy = new Point(pp.x + horizontalStepX * factor,
                        pp.y + horizontalStepY * factor);
                Point ppxy = new Point(pp.x + verticalStepX * factor+ horizontalStepX * factor,
                        pp.y + verticalStepY * factor + horizontalStepY * factor);
                MatOfPoint points = new MatOfPoint(pp, ppx, ppxy, ppy);
                Imgproc.fillPoly(frame, Collections.singletonList(points), getColorByCubeColor(color));
            }
        }
        return frame;
    }

    ColorDetector detector = new ColorDetector();
    public List<Quadrilateral> getAllQuad(Point p1, Point p2, Point p3, Point p4) {
        List<Quadrilateral> quadrilaterals = new ArrayList<>();
        int numGrid = 3;
        // Calculate step sizes for horizontal and vertical lines
        double horizontalStepX = (p2.x - p1.x) / numGrid;
        double horizontalStepY = (p2.y - p1.y) / numGrid;
        double verticalStepX = (p4.x - p1.x) / numGrid;
        double verticalStepY = (p4.y - p1.y) / numGrid;

        for (int hIndex = 0; hIndex < numGrid; hIndex++) {
            // Draw vertical grid lines
            for (int vIndex = 0; vIndex < numGrid; vIndex++) {
                Point pp = new Point(p1.x + verticalStepX * hIndex + horizontalStepX * vIndex,
                        p1.y + verticalStepY * hIndex + horizontalStepY * vIndex);
                Point ppx = new Point(pp.x + verticalStepX,
                        pp.y + verticalStepY);
                Point ppy = new Point(pp.x + horizontalStepX,
                        pp.y + horizontalStepY);
                Point ppxy = new Point(pp.x + verticalStepX + horizontalStepX,
                        pp.y + verticalStepY + horizontalStepY);
                quadrilaterals.add(
                        new Quadrilateral(pp, ppx, ppxy, ppy, hIndex, vIndex)
                );
            }
        }
        return quadrilaterals;
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
