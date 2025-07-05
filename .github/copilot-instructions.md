Luôn trả lời tôi bằng tiếng việt
Khi yêu cầu lập kế hoạch, luôn tạo trong đường dẫn plans/

## THÔNG TIN DỰ ÁN KUBESOLVER

### Mô tả dự án
KubeSolver là một ứng dụng Android giải Rubik's Cube tự động sử dụng camera và OpenCV. Ứng dụng có thể:
- Nhận diện màu sắc của Rubik's Cube qua camera
- Xử lý hình ảnh để detect các mặt của cube
- Tính toán thuật toán giải cube
- Hiển thị cube 3D và animation các bước giải

### Thông tin kỹ thuật
- **Package name**: com.khainv9.kubesolver
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35
- **Version**: 1.0 (versionCode: 1)
- **Language**: Kotlin + Java + C++
- **Build system**: Gradle với Kotlin DSL

### Kiến trúc dự án
1. **Android App (Kotlin/Java)**
   - `CameraActivity`: Activity chính xử lý camera và UI
   - `ColorDetector`: Nhận diện màu sắc từ camera
   - `PhaseControl`: Điều khiển các phase của việc scan cube
   - `CubeRenderer`: Render cube 3D với OpenGL ES
   - `CubeView`: Custom view hiển thị cube 3D

2. **Native C++ Algorithm**
   - `solver.cpp/.h`: Core solving algorithm
   - `blockcube.cpp/.h`: Biểu diễn trạng thái cube
   - `colorfulcube.cpp/.h`: Cube với thông tin màu sắc
   - `rotationphase.cpp/.h`: Phase 1 của thuật toán giải
   - `permutationphase.cpp/.h`: Phase 2 của thuật toán giải
   - `move.cpp/.h`: Định nghĩa các move của cube

3. **OpenCV Integration**
   - Sử dụng OpenCV 4.5.5 cho xử lý hình ảnh
   - Camera processing và color detection
   - Geometric transformations

### Thư viện và dependencies chính
- OpenCV 4.5.5 (com.quickbirdstudios:opencv:3.4.1)
- Android CameraX (camera-core, camera-camera2, camera-lifecycle)
- AndroidX (core-ktx, appcompat, constraintlayout)
- Material Design
- Navigation components (navigation-fragment-ktx, navigation-ui-ktx)

### Cấu trúc thư mục quan trọng
```
app/src/main/
├── java/com/khainv9/kubesolver/
│   ├── camera/              # Camera và color detection
│   └── cubeview/            # 3D cube rendering
├── cpp/                     # Native C++ algorithm
│   ├── algorithm/           # Core solving algorithms
│   ├── CMakeLists.txt       # CMake build config
│   └── native-lib.cpp       # JNI interface
└── res/                     # Android resources
```

### Thuật toán giải cube
- Sử dụng thuật toán two-phase algorithm
- Phase 1 (Rotation): Đưa cube về trạng thái G1
- Phase 2 (Permutation): Giải hoàn toàn cube từ G1
- Tối ưu hóa với lookup tables để tăng tốc

### Build và CMake
- CMake version: 3.22.1
- C++ standard: C++14
- Native library: "kubesolver"
- External native build path: src/main/cpp/CMakeLists.txt

### Tính năng chính
1. **Camera scanning**: Scan 6 mặt của cube
2. **Color detection**: Nhận diện 6 màu (Red, Green, Blue, Yellow, Orange, White)
3. **3D visualization**: Hiển thị cube 3D với OpenGL
4. **Animation**: Animate các move của cube
5. **Solving algorithm**: Tính toán các bước giải tối ưu

### Permissions
- CAMERA: Để truy cập camera device
- Camera hardware features: Tự động focus, front/back camera

### Main Activity
- `CameraActivity`: Landscape orientation, theme tùy chỉnh
- Sử dụng ViewBinding cho UI
- Implement OpenCV camera callbacks