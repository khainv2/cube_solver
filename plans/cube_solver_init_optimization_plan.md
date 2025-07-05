# Kế hoạch tối ưu hóa hàm init() cho KubeSolver

## 1. Phân tích hiện trạng

### Vấn đề chính:
- Hàm `kube::init()` phải tính toán và lưu trữ bảng lookup khổng lồ
- Rotation phase: ~2-3 triệu entries trong EndPhaseSearchTable
- Permutation phase: ~2-3 triệu entries trong EndPhaseSearchTable  
- Thời gian khởi tạo có thể lên đến vài phút

### Cấu trúc dữ liệu hiện tại:

#### Rotation Phase:
- `EdgeStateList`: 2^11 = 2,048 states × 18 moves = 36,864 entries
- `CornerStateList`: 3^7 = 2,187 states × 18 moves = 39,366 entries
- `EdgePosList`: C(12,4) = 495 states × 18 moves = 8,910 entries
- `EndPhaseSearchTable`: BFS đến level 7 → ~2.7 triệu entries

#### Permutation Phase:
- `UpDownEdgeStateList`: 8! = 40,320 states × 10 moves = 403,200 entries
- `MidEdgeStateList`: 4! = 24 states × 10 moves = 240 entries
- `CornerStateList`: 8! = 40,320 states × 10 moves = 403,200 entries
- `EndPhaseSearchTable`: BFS đến level 8 → ~2.8 triệu entries

## 2. Chiến lược tối ưu hóa

### 2.1 Tối ưu hóa ngay lập tức (Quick wins)

#### A. Thêm cache file cho Permutation Phase
**Mức độ ảnh hưởng**: ⭐⭐⭐⭐⭐
**Độ khó**: ⭐⭐
**Thời gian**: 2-3 giờ

```cpp
// Thêm vào permutationphase.h
static void save_end_phase_to_file() {
    std::ofstream wf("perm.dat", std::ios::out | std::ios::binary);
    // Implement tương tự như rotation phase
}

static void load_end_phase_from_file() {
    std::ifstream rf("perm.dat", std::ios::in | std::ios::binary);
    // Implement tương tự như rotation phase
}
```

#### B. Giảm InitTableLevelMax
**Mức độ ảnh hưởng**: ⭐⭐⭐⭐
**Độ khó**: ⭐
**Thời gian**: 30 phút

```cpp
// Trong rotationphase.h
enum {
    InitTableLevelMax = 6, // Giảm từ 7 xuống 6
    SearchLevelMax = 6
};

// Trong permutationphase.h  
enum {
    InitTableLevelMax = 7, // Giảm từ 8 xuống 7
    SearchLevelMax = 12
};
```

**Trade-off**: Thời gian init giảm ~60-70%, thời gian solve tăng ~10-15%

### 2.2 Tối ưu hóa trung hạn (Medium term)

#### A. Parallel Computing cho BFS
**Mức độ ảnh hưởng**: ⭐⭐⭐⭐
**Độ khó**: ⭐⭐⭐
**Thời gian**: 1-2 ngày

```cpp
#include <thread>
#include <atomic>
#include <mutex>

// Chia BFS thành multiple threads
void init_lookup_table_parallel() {
    const int num_threads = std::thread::hardware_concurrency();
    std::vector<std::thread> threads;
    std::mutex table_mutex;
    
    // Chia data theo threads
    for (int t = 0; t < num_threads; t++) {
        threads.emplace_back([&, t]() {
            // Process subset of states
        });
    }
    
    for (auto& thread : threads) {
        thread.join();
    }
}
```

#### B. Memory Mapping cho file cache
**Mức độ ảnh hưởng**: ⭐⭐⭐
**Độ khó**: ⭐⭐⭐
**Thời gian**: 1 ngày

```cpp
#include <sys/mman.h>
#include <fcntl.h>

// Sử dụng memory mapping thay vì file I/O
void* map_cache_file(const char* filename, size_t size) {
    int fd = open(filename, O_RDONLY);
    void* mapped = mmap(nullptr, size, PROT_READ, MAP_SHARED, fd, 0);
    close(fd);
    return mapped;
}
```

#### C. Tối ưu hóa cấu trúc dữ liệu
**Mức độ ảnh hưởng**: ⭐⭐⭐
**Độ khó**: ⭐⭐⭐
**Thời gian**: 1-2 ngày

```cpp
// Sử dụng packed struct để giảm memory footprint
struct __attribute__((packed)) CompactEntry {
    uint32_t state;
    uint16_t moves;
};

// Sử dụng hash table tối ưu
#include <google/dense_hash_map>
using FastHashMap = google::dense_hash_map<uint64, MutableMoveList>;
```

### 2.3 Tối ưu hóa dài hạn (Long term)

#### A. Lazy Loading + On-demand computation
**Mức độ ảnh hưởng**: ⭐⭐⭐⭐⭐
**Độ khó**: ⭐⭐⭐⭐
**Thời gian**: 3-5 ngày

```cpp
class LazyLookupTable {
    std::unordered_map<uint64, MutableMoveList> cache;
    std::atomic<bool> computing{false};
    
public:
    MutableMoveList get(uint64 state) {
        if (auto it = cache.find(state); it != cache.end()) {
            return it->second;
        }
        
        // Compute on-demand
        return compute_and_cache(state);
    }
    
private:
    MutableMoveList compute_and_cache(uint64 state) {
        // BFS từ state cho đến khi tìm thấy solved state
        // Cache kết quả
    }
};
```

#### B. Compression algorithms
**Mức độ ảnh hưởng**: ⭐⭐⭐
**Độ khó**: ⭐⭐⭐⭐
**Thời gian**: 2-3 ngày

```cpp
#include <zlib.h>
#include <lz4.h>

// Nén dữ liệu lookup table
void compress_lookup_table() {
    // Sử dụng LZ4 hoặc Zlib để nén
    // Trade-off: CPU time vs Memory usage
}
```

#### C. Symmetry reduction
**Mức độ ảnh hưởng**: ⭐⭐⭐⭐
**Độ khó**: ⭐⭐⭐⭐⭐
**Thời gian**: 1-2 tuần

```cpp
// Sử dụng symmetry của cube để giảm số states cần lưu
class SymmetryReducedTable {
    // Chỉ lưu canonical states
    // Sử dụng symmetry transformation để map về canonical form
};
```

## 3. Kế hoạch triển khai

### Giai đoạn 1 (Tuần 1): Quick wins
1. **Ngày 1-2**: Implement cache file cho permutation phase
2. **Ngày 3**: Test và điều chỉnh InitTableLevelMax
3. **Ngày 4-5**: Profiling và measurement

### Giai đoạn 2 (Tuần 2-3): Medium optimizations  
1. **Tuần 2**: Parallel computing cho BFS
2. **Tuần 3**: Memory mapping và data structure optimization

### Giai đoạn 3 (Tuần 4-6): Advanced optimizations
1. **Tuần 4-5**: Lazy loading system
2. **Tuần 6**: Compression và symmetry reduction research

## 4. Kết quả kỳ vọng

### Sau giai đoạn 1:
- Thời gian init giảm: **60-70%** (từ ~5 phút xuống ~1.5 phút)
- Thời gian solve tăng: **10-15%** (acceptable trade-off)

### Sau giai đoạn 2:
- Thời gian init giảm thêm: **30-40%** (xuống ~1 phút)
- Memory usage giảm: **20-30%**

### Sau giai đoạn 3:
- Thời gian init giảm: **80-90%** (xuống ~30 giây)
- Thời gian solve: **Tương đương hoặc nhanh hơn**
- Memory usage giảm: **50-60%**

## 5. Risk Assessment

### Risks cao:
- **Lazy loading**: Có thể gây lag trong lần solve đầu tiên
- **Symmetry reduction**: Rất phức tạp, có thể introduce bugs

### Risks trung bình:
- **Parallel computing**: Threading bugs, race conditions
- **Data compression**: CPU overhead

### Risks thấp:
- **Cache files**: File corruption, disk space
- **Reduce table levels**: Slight performance degradation

## 6. Metrics để theo dõi

### Performance metrics:
- **Init time**: Thời gian khởi tạo (ms)
- **Solve time**: Thời gian giải trung bình (ms)
- **Memory usage**: Peak memory usage (MB)
- **File size**: Kích thước cache files (MB)

### Quality metrics:
- **Solve quality**: Số move trung bình để solve
- **Success rate**: Tỷ lệ solve thành công
- **Stability**: Số lần crash/error

## 7. Monitoring và Testing

### Unit tests:
```cpp
TEST(SolverOptimization, InitTimeImprovement) {
    auto start = std::chrono::high_resolution_clock::now();
    kube::init();
    auto end = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - start);
    
    EXPECT_LT(duration.count(), 60000); // < 1 phút
}

TEST(SolverOptimization, SolveQualityMaintained) {
    // Test solve quality không bị giảm đáng kể
}
```

### Benchmarks:
```cpp
void benchmark_init() {
    for (int i = 0; i < 10; i++) {
        auto start = std::chrono::high_resolution_clock::now();
        kube::init();
        auto end = std::chrono::high_resolution_clock::now();
        // Log timing
    }
}
```

## 8. Kết luận

Kế hoạch tối ưu hóa này sẽ giúp giảm đáng kể thời gian khởi tạo của KubeSolver từ vài phút xuống dưới 1 phút, với trade-off minimal về chất lượng giải cube. Việc implement theo từng giai đoạn sẽ giúp kiểm soát risk và đảm bảo stability của hệ thống.
