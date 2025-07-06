# KẾ HOẠCH XÂY DỰNG MAIN ACTIVITY CHO KUBESOLVER

## 1. TỔNG QUAN KIẾN TRÚC

### 1.1 Mục tiêu
Xây dựng MainActivity như một activity chính thay thế CameraActivity hiện tại, tích hợp đầy đủ các tính năng:
- Camera quét Rubik's Cube
- Hiển thị trạng thái 3D của cube
- Giải cube và hiển thị solution
- Media control cho animation từng bước

### 1.2 Kiến trúc Component
```
MainActivity
├── CameraView (OpenCV)
├── CubeGLView (OpenGL ES)
├── MediaControlView (Custom)
├── StatusView (Info/Progress)
└── SolverManager (JNI Bridge)
```

## 2. THIẾT KẾ GIAO DIỆN

### 2.1 Layout Structure
```
┌─────────────────────────────────────┐
│           STATUS BAR                │
├─────────────────────────────────────┤
│                                     │
│         CAMERA VIEW                 │
│    (Scan Rubik's Cube)             │
│                                     │
├─────────────────────────────────────┤
│                                     │
│         3D CUBE VIEW                │
│      (OpenGL Display)              │
│                                     │
├─────────────────────────────────────┤
│         MEDIA CONTROLS              │
│  [<<] [Play/Pause] [>>] Progress    │
└─────────────────────────────────────┘
```

### 2.2 UI Components

#### 2.2.1 StatusView
```java
public class StatusView extends LinearLayout {
    private TextView statusText;
    private ProgressBar progressBar;
    private ImageView statusIcon;
    
    // Các trạng thái
    enum ScanStatus {
        IDLE,           // Chưa bắt đầu
        SCANNING,       // Đang quét
        FACE_COMPLETE,  // Hoàn thành 1 mặt
        ALL_COMPLETE,   // Hoàn thành tất cả
        SOLVING,        // Đang giải
        SOLVED,         // Đã giải xong
        ERROR           // Lỗi
    }
}
```

#### 2.2.2 CameraView (Enhanced)
```java
public class EnhancedCameraView extends CameraBridgeViewBase {
    private ScanPhaseManager phaseManager;
    private ColorDetector colorDetector;
    private OverlayRenderer overlayRenderer;
    
    // Cải tiến từ CameraActivity
    - Tối ưu color detection
    - Thêm overlay instructions
    - Auto capture mode
    - Feedback haptic
}
```

#### 2.2.3 MediaControlView
```java
public class MediaControlView extends LinearLayout {
    private ImageButton btnPrevious;
    private ImageButton btnPlayPause;
    private ImageButton btnNext;
    private SeekBar progressBar;
    private TextView stepInfo;
    
    // Chức năng
    - Play/Pause animation
    - Skip to specific step
    - Speed control
    - Step by step mode
}
```

## 3. QUẢN LÝ TRẠNG THÁI

### 3.1 State Machine
```java
public enum AppState {
    SCAN_READY,      // Sẵn sàng quét
    SCANNING,        // Đang quét cube
    SCAN_COMPLETE,   // Quét xong, sẵn sàng giải
    SOLVING,         // Đang tính toán solution
    SOLUTION_READY,  // Có solution, sẵn sàng animate
    ANIMATING,       // Đang chạy animation
    COMPLETED        // Hoàn thành
}
```

### 3.2 ScanPhaseManager (Enhanced)
```java
public class ScanPhaseManager {
    private CubeState cubeState;
    private int currentFace = 0;
    private boolean[] faceCompleted = new boolean[6];
    
    // Cải tiến từ PhaseControl
    public void updateFaceColors(CubeColor[] colors, Face face);
    public Direction getNextScanDirection();
    public boolean isAllFacesScanned();
    public ColorfulCube getCompleteCube();
}
```

## 4. TÍCH HỢP NATIVE SOLVER

### 4.1 SolverManager (JNI Bridge)
```java
public class SolverManager {
    // Native methods
    public native String solveCube(String cubeStateString);
    public native boolean validateCube(String cubeStateString);
    public native int getMinimalMoves(String cubeStateString);
    
    // Java wrapper methods
    public SolutionResult solve(ColorfulCube cube) {
        String cubeStr = cubeToString(cube);
        String solution = solveCube(cubeStr);
        return new SolutionResult(solution);
    }
}
```

### 4.2 Native Interface Enhancement
```cpp
// Thêm vào native-lib.cpp
extern "C"
JNIEXPORT jstring JNICALL
Java_com_khainv9_kubesolver_SolverManager_solveCube(
        JNIEnv *env,
        jobject /* this */,
        jstring cubeState) {
    
    const char *cubeStr = env->GetStringUTFChars(cubeState, 0);
    
    // Sử dụng existing solver
    Solver solver;
    std::string solution = solver.solve(cubeStr);
    
    env->ReleaseStringUTFChars(cubeState, cubeStr);
    return env->NewStringUTF(solution.c_str());
}
```

## 5. ANIMATION SYSTEM

### 5.1 MoveAnimator
```java
public class MoveAnimator {
    private List<Move> moveSequence;
    private int currentMoveIndex;
    private RubiksCubeGLSurfaceView cubeView;
    private MediaControlView mediaControl;
    
    public void setMoveSequence(List<Move> moves);
    public void play();
    public void pause();
    public void seekToMove(int index);
    public void setSpeed(float speed);
}
```

### 5.2 Animation Features
- **Play/Pause**: Điều khiển animation
- **Step by step**: Đi từng nước một
- **Speed control**: Tốc độ animation (0.5x - 2x)
- **Progress tracking**: Hiển thị tiến độ
- **Jump to step**: Nhảy đến bước cụ thể

## 6. IMPLEMENTATION PLAN

### 6.1 Phase 1: Basic Structure (Tuần 1-2)
1. **Tạo MainActivity layout**
   - Thiết kế XML layout
   - Tạo các custom views
   - Setup basic navigation

2. **Chuyển đổi từ CameraActivity**
   - Copy camera functionality
   - Refactor code structure
   - Update imports và dependencies

### 6.2 Phase 2: Enhanced Camera (Tuần 3-4)
1. **Cải tiến camera scanning**
   - Tối ưu color detection
   - Thêm visual feedback
   - Auto-capture mode
   - Validation và error handling

2. **State management**
   - Implement ScanPhaseManager
   - Add status indicators
   - Face completion tracking

### 6.3 Phase 3: Solver Integration (Tuần 5-6)
1. **Native solver bridge**
   - Tạo SolverManager
   - JNI interface
   - Cube state conversion
   - Error handling

2. **Solution processing**
   - Parse move strings
   - Validate solutions
   - Optimization suggestions

### 6.4 Phase 4: Media Controls (Tuần 7-8)
1. **Animation system**
   - MoveAnimator implementation
   - Timeline control
   - Speed adjustment
   - Step navigation

2. **Media controls UI**
   - Custom media bar
   - Progress indicator
   - Control buttons
   - Status display

### 6.5 Phase 5: Polish & Testing (Tuần 9-10)
1. **UI/UX improvements**
   - Animations và transitions
   - Error messages
   - User guidance
   - Accessibility

2. **Testing & optimization**
   - Performance tuning
   - Memory management
   - Edge case handling
   - User testing

## 7. DETAILED IMPLEMENTATION

### 7.1 MainActivity.java Structure
```java
public class MainActivity extends AppCompatActivity {
    // Views
    private EnhancedCameraView cameraView;
    private RubiksCubeGLSurfaceView cubeView;
    private MediaControlView mediaControl;
    private StatusView statusView;
    
    // Managers
    private ScanPhaseManager scanManager;
    private SolverManager solverManager;
    private MoveAnimator animator;
    
    // State
    private AppState currentState;
    private ColorfulCube scannedCube;
    private List<Move> solution;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initManagers();
        setupEventListeners();
        initOpenCV();
    }
    
    private void initViews() {
        cameraView = findViewById(R.id.camera_view);
        cubeView = findViewById(R.id.cube_view);
        mediaControl = findViewById(R.id.media_control);
        statusView = findViewById(R.id.status_view);
        
        // Setup camera
        cameraView.setCvCameraViewListener(this);
        cameraView.setScanCallback(this::onFaceScanned);
        
        // Setup cube view
        cubeView.setOnMoveCompleteListener(this::onMoveComplete);
        
        // Setup media control
        mediaControl.setOnControlListener(this::onMediaControl);
        mediaControl.setVisibility(View.GONE);
    }
    
    private void onFaceScanned(CubeColor[] colors, Face face) {
        scanManager.updateFaceColors(colors, face);
        updateScanStatus();
        
        if (scanManager.isAllFacesScanned()) {
            scannedCube = scanManager.getCompleteCube();
            cubeView.updateCube(scannedCube);
            setState(AppState.SCAN_COMPLETE);
        }
    }
    
    private void solveCube() {
        setState(AppState.SOLVING);
        
        // Solve in background thread
        new Thread(() -> {
            try {
                SolutionResult result = solverManager.solve(scannedCube);
                solution = result.getMoves();
                
                runOnUiThread(() -> {
                    mediaControl.setMoveSequence(solution);
                    mediaControl.setVisibility(View.VISIBLE);
                    setState(AppState.SOLUTION_READY);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Không thể giải cube: " + e.getMessage());
                    setState(AppState.SCAN_COMPLETE);
                });
            }
        }).start();
    }
    
    private void onMediaControl(MediaControl.Action action) {
        switch (action) {
            case PLAY:
                animator.play();
                setState(AppState.ANIMATING);
                break;
            case PAUSE:
                animator.pause();
                setState(AppState.SOLUTION_READY);
                break;
            case PREVIOUS:
                animator.previousMove();
                break;
            case NEXT:
                animator.nextMove();
                break;
            case SEEK:
                animator.seekToMove(mediaControl.getProgress());
                break;
        }
    }
    
    private void setState(AppState newState) {
        currentState = newState;
        updateUI();
    }
    
    private void updateUI() {
        switch (currentState) {
            case SCAN_READY:
                statusView.setStatus("Đặt cube vào khung và bắt đầu quét", StatusView.ScanStatus.IDLE);
                break;
            case SCANNING:
                statusView.setStatus("Đang quét mặt " + (scanManager.getCurrentFace() + 1) + "/6", StatusView.ScanStatus.SCANNING);
                break;
            case SCAN_COMPLETE:
                statusView.setStatus("Quét hoàn tất! Nhấn 'Giải' để bắt đầu", StatusView.ScanStatus.ALL_COMPLETE);
                break;
            case SOLVING:
                statusView.setStatus("Đang tính toán cách giải...", StatusView.ScanStatus.SOLVING);
                break;
            case SOLUTION_READY:
                statusView.setStatus("Sẵn sàng! " + solution.size() + " bước để giải", StatusView.ScanStatus.SOLVED);
                break;
            case ANIMATING:
                statusView.setStatus("Đang thực hiện bước " + (animator.getCurrentStep() + 1) + "/" + solution.size(), StatusView.ScanStatus.SOLVING);
                break;
            case COMPLETED:
                statusView.setStatus("Đã giải xong! Chúc mừng!", StatusView.ScanStatus.SOLVED);
                break;
        }
    }
}
```

### 7.2 Layout Files

#### 7.2.1 activity_main.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!-- Status Bar -->
    <com.khainv9.kubesolver.ui.StatusView
        android:id="@+id/status_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp" />

    <!-- Camera View -->
    <com.khainv9.kubesolver.camera.EnhancedCameraView
        android:id="@+id/camera_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <!-- 3D Cube View -->
    <com.khainv9.kubesolver.cubeview.RubiksCubeGLSurfaceView
        android:id="@+id/cube_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <!-- Media Controls -->
    <com.khainv9.kubesolver.ui.MediaControlView
        android:id="@+id/media_control"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp"
        android:visibility="gone" />

    <!-- Action Buttons -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp">

        <Button
            android:id="@+id/btn_reset"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Reset"
            android:layout_marginEnd="8dp" />

        <Button
            android:id="@+id/btn_solve"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Giải Cube"
            android:layout_marginStart="8dp" />

    </LinearLayout>

</LinearLayout>
```

## 8. TESTING STRATEGY

### 8.1 Unit Tests
- ScanPhaseManager logic
- SolverManager integration
- MoveAnimator functionality
- State transitions

### 8.2 Integration Tests
- Camera → Cube detection
- Cube → Solver → Animation
- UI state management
- Performance benchmarks

### 8.3 User Testing
- Scanning accuracy
- Animation smoothness
- UI responsiveness
- Error handling

## 9. PERFORMANCE CONSIDERATIONS

### 9.1 Camera Processing
- Reduce frame processing rate
- Optimize color detection algorithms
- Use background threads for heavy operations

### 9.2 3D Rendering
- Optimize OpenGL calls
- Efficient vertex buffer usage
- Texture caching

### 9.3 Memory Management
- Proper Mat cleanup in OpenCV
- Native memory management
- View recycling

## 10. FUTURE ENHANCEMENTS

### 10.1 Advanced Features
- Multiple solving algorithms
- Optimal solution finding
- Pattern recognition
- Tutorial mode

### 10.2 UI Improvements
- Dark mode support
- Themes và customization
- Accessibility features
- Multi-language support

### 10.3 Performance
- GPU acceleration
- Machine learning color detection
- Predictive scanning
- Cloud solving service

---

*Kế hoạch này dựa trên kiến trúc hiện tại của KubeSolver và tích hợp các tính năng mới một cách có hệ thống.*
