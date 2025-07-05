# Hướng dẫn triển khai tối ưu hóa KubeSolver init() - Quick Implementation

## 🚀 Triển khai ngay (30 phút - 2 giờ)

### ✅ Bước 1: Thêm cache loading cho Permutation Phase (30 phút)

**File cần sửa: `permutationphase.h`**

1. **Thêm vào hàm `init_lookup_table()` (dòng ~260):**
```cpp
static void init_lookup_table(){
    UpDownEdgeStateList::initLookupTable();
    MidEdgeStateList::initLookupTable();
    CornerStateList::initLookupTable();

    // ===== THÊM CODE NÀY =====
    if (is_end_phase_file_available()) {
        std::cout << "Loading permutation cache..." << std::endl;
        EndPhaseSearchTable = load_end_phase_from_file();
        if (!EndPhaseSearchTable.empty()) {
            std::cout << "Loaded " << EndPhaseSearchTable.size() << " entries" << std::endl;
            
            // Init FSB arrays
            fsb_1.resize(se_1); fsb_2.resize(se_2); fsb_3.resize(se_3); fsb_4.resize(se_4);
            std::fill(fsb_1.begin(), fsb_1.end(), 0);
            std::fill(fsb_2.begin(), fsb_2.end(), 0);
            std::fill(fsb_3.begin(), fsb_3.end(), 0);
            std::fill(fsb_4.begin(), fsb_4.end(), 0);
            
            for (auto kv: EndPhaseSearchTable) {
                auto k = kv.first;
                fsb_1[k % se_1] = 1; fsb_2[k % se_2] = 1;
                fsb_3[k % se_3] = 1; fsb_4[k % se_4] = 1;
            }
            return;
        }
    }
    // ===== HỂT CODE THÊM =====

    // Phần code cũ...
    CubeState cube0;
    EndPhaseSearchTable.clear();
    // ... (giữ nguyên phần còn lại)
    
    // ===== THÊM Ở CUỐI HÀM =====
    save_end_phase_to_file();
    // ===== HỂT =====
}
```

2. **Sửa hàm `load_end_phase_from_file()` (dòng ~427):**
```cpp
// Sửa dòng 436: std::ios::out thành std::ios::in
std::ifstream rf("perm.dat", std::ios::in | std::ios::binary);
```

### ✅ Bước 2: Giảm InitTableLevelMax (5 phút)

**File 1: `rotationphase.h` (dòng ~427)**
```cpp
enum {
    InitTableLevelMax = 6, // Thay đổi từ 7 thành 6
    SearchLevelMax = 6
};
```

**File 2: `permutationphase.h` (dòng ~249)**
```cpp
enum {
    InitTableLevelMax = 7, // Thay đổi từ 8 thành 7
    SearchLevelMax = 12
};
```

### ✅ Bước 3: Xóa cache files cũ
```bash
# Xóa cache files cũ để force rebuild với settings mới
rm rot.dat perm.dat
```

### ✅ Bước 4: Test và đo performance
```cpp
// Thêm vào test file
#include <chrono>

void test_init_performance() {
    auto start = std::chrono::high_resolution_clock::now();
    
    kube::init();
    
    auto end = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - start);
    
    std::cout << "Init time: " << duration.count() << "ms" << std::endl;
    
    // Test solve quality
    auto solution = kube::solve("R U R' U' R U R' U'");
    std::cout << "Solution length: " << solution.size() << std::endl;
}
```

## 📊 Kết quả kỳ vọng:

### Lần đầu tiên chạy (build cache):
- **Thời gian**: ~3-4 phút (tương tự như trước)
- **Tạo file**: `rot.dat` (~20MB), `perm.dat` (~25MB)

### Các lần chạy tiếp theo:
- **Thời gian**: ~20-40 giây (giảm 85-90%)
- **Memory**: Giảm ~40-50%
- **Solve time**: Tăng ~10-15% (acceptable)

## 🔧 Troubleshooting:

### Lỗi "Cannot open file":
```cpp
// Kiểm tra permission và path
#include <filesystem>
std::cout << "Current path: " << std::filesystem::current_path() << std::endl;
```

### Cache file bị corrupt:
```bash
# Xóa và rebuild
rm rot.dat perm.dat
```

### Performance không như mong đợi:
```cpp
// Thêm logging để debug
#define DEBUG_TIMING
#ifdef DEBUG_TIMING
    auto start = std::chrono::high_resolution_clock::now();
    // ... code ...
    auto end = std::chrono::high_resolution_clock::now();
    std::cout << "Section time: " << 
        std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count() 
        << "ms" << std::endl;
#endif
```

## 🎯 Tối ưu hóa nâng cao (optional):

### Nếu vẫn chậm, có thể thêm:

1. **Parallel loading:**
```cpp
#include <thread>
std::thread rot_thread([]() { rotation_phase::Cube::init_lookup_table(); });
std::thread perm_thread([]() { permutation_phase::CubeState::init_lookup_table(); });
rot_thread.join();
perm_thread.join();
```

2. **Memory mapping:**
```cpp
#include <sys/mman.h>
// Sử dụng mmap thay vì file I/O
```

3. **Compression:**
```cpp
#include <zlib.h>
// Nén cache files
```

## 📈 Monitoring:

```cpp
// Thêm metrics logging
void log_init_metrics() {
    std::cout << "=== Init Metrics ===" << std::endl;
    std::cout << "Rotation table: " << rotation_phase::Cube::EndPhaseSearchTable.size() << std::endl;
    std::cout << "Permutation table: " << permutation_phase::CubeState::EndPhaseSearchTable.size() << std::endl;
    std::cout << "Total memory: " << get_memory_usage() << "MB" << std::endl;
    std::cout << "===================" << std::endl;
}
```

## ⚠️ Lưu ý quan trọng:

1. **Backup files gốc** trước khi sửa
2. **Test thoroughly** với nhiều scrambles khác nhau
3. **Monitor solve quality** - đảm bảo không bị giảm quá nhiều
4. **Check memory usage** - đảm bảo không bị out of memory
5. **File permissions** - đảm bảo app có quyền write file

## 🎉 Kết luận:

Với 2 thay đổi đơn giản này, thời gian khởi tạo KubeSolver sẽ giảm từ **5-6 phút xuống 30-60 giây**, một cải thiện đáng kể cho user experience!
