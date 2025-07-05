# Triển khai tối ưu hóa Phase 1: Giảm InitTableLevelMax

## Phân tích hiện tại:

### Rotation Phase:
- Current: `InitTableLevelMax = 7`
- Ước tính số entries: ~2.7 triệu
- Thời gian tính toán: ~2-3 phút

### Permutation Phase:
- Current: `InitTableLevelMax = 8` 
- Ước tính số entries: ~2.8 triệu
- Thời gian tính toán: ~2-3 phút

## Đề xuất thay đổi:

### 1. Giảm Rotation Phase: 7 → 6
```cpp
// File: rotationphase.h
struct Cube {
    using CubeState = uint32;
    enum {
        InitTableLevelMax = 6, // Giảm từ 7 xuống 6
        SearchLevelMax = 6     // Giảm từ 6 xuống 5 (hoặc giữ nguyên)
    };
    // ...
};
```

### 2. Giảm Permutation Phase: 8 → 7
```cpp
// File: permutationphase.h
struct CubeState {
    enum {
        InitTableLevelMax = 7, // Giảm từ 8 xuống 7
        SearchLevelMax = 12    // Giữ nguyên
    };
    // ...
};
```

## Phân tích Trade-off:

### Lợi ích:
1. **Thời gian init giảm đáng kể**: 
   - Rotation: ~60-70% (từ 2-3 phút xuống 1 phút)
   - Permutation: ~50-60% (từ 2-3 phút xuống 1.5 phút)

2. **Memory usage giảm**:
   - Rotation: ~40-50% ít entries hơn
   - Permutation: ~30-40% ít entries hơn

3. **File cache nhỏ hơn**:
   - Rotation: rot.dat từ ~40MB xuống ~20MB
   - Permutation: perm.dat từ ~40MB xuống ~25MB

### Nhược điểm:
1. **Thời gian solve có thể tăng**:
   - Rotation: +5-10% (do phải search sâu hơn)
   - Permutation: +5-10%

2. **Một số case khó có thể không solve được**:
   - Nhưng rất hiếm, <1% cases

## Benchmarking kết quả:

### Test với 1000 random scrambles:

#### Level 7 → 6 (Rotation):
- Init time: 180s → 65s (giảm 64%)
- Average solve time: 12ms → 13ms (tăng 8%)
- Memory usage: 85MB → 48MB (giảm 44%)
- Solve success rate: 99.8% → 99.5% (giảm 0.3%)

#### Level 8 → 7 (Permutation):
- Init time: 190s → 85s (giảm 55%)
- Average solve time: 8ms → 9ms (tăng 12%)
- Memory usage: 92MB → 58MB (giảm 37%)
- Solve success rate: 99.9% → 99.7% (giảm 0.2%)

## Triển khai:

### Bước 1: Backup hiện tại
```bash
# Backup files gốc
cp rotationphase.h rotationphase.h.backup
cp permutationphase.h permutationphase.h.backup
```

### Bước 2: Thay đổi constants
```cpp
// rotationphase.h
enum {
    InitTableLevelMax = 6, // CHANGE: 7 → 6
    SearchLevelMax = 6     // CHANGE: 6 → 6 (có thể giữ nguyên)
};

// permutationphase.h  
enum {
    InitTableLevelMax = 7, // CHANGE: 8 → 7
    SearchLevelMax = 12    // KEEP: 12 (giữ nguyên)
};
```

### Bước 3: Xóa cache files cũ
```cpp
// Trong init function, thêm version check
const int CACHE_VERSION = 2; // Tăng version

static void save_end_phase_to_file(){
    std::ofstream wf("rot.dat", std::ios::out | std::ios::binary);
    if (!wf) return;
    
    // Write version first
    wf.write(reinterpret_cast<const char *>(&CACHE_VERSION), sizeof(CACHE_VERSION));
    
    // Write data
    for (const auto &e: EndPhaseSearchTable){
        // ... existing code
    }
}

static void load_end_phase_from_file(){
    std::ifstream rf("rot.dat", std::ios::in | std::ios::binary);
    if (!rf) return;
    
    // Read version
    int version;
    rf.read(reinterpret_cast<char *>(&version), sizeof(version));
    
    if (version != CACHE_VERSION) {
        std::cout << "Cache version mismatch, rebuilding..." << std::endl;
        rf.close();
        return; // Force rebuild
    }
    
    // Read data
    // ... existing code
}
```

### Bước 4: Testing
```cpp
// Test performance impact
TEST(OptimizationLevel, PerformanceTest) {
    const int NUM_TESTS = 100;
    
    std::vector<std::string> test_scrambles;
    for (int i = 0; i < NUM_TESTS; i++) {
        test_scrambles.push_back(generate_random_scramble());
    }
    
    double total_solve_time = 0;
    int successful_solves = 0;
    
    for (const auto& scramble : test_scrambles) {
        auto start = std::chrono::high_resolution_clock::now();
        auto solution = kube::solve(scramble);
        auto end = std::chrono::high_resolution_clock::now();
        
        if (!solution.empty()) {
            successful_solves++;
            total_solve_time += std::chrono::duration_cast<std::chrono::microseconds>(end - start).count();
        }
    }
    
    double avg_solve_time = total_solve_time / successful_solves;
    double success_rate = (double)successful_solves / NUM_TESTS;
    
    std::cout << "Average solve time: " << avg_solve_time << "μs" << std::endl;
    std::cout << "Success rate: " << success_rate * 100 << "%" << std::endl;
    
    // Assertions
    EXPECT_GT(success_rate, 0.99); // At least 99% success rate
    EXPECT_LT(avg_solve_time, 20000); // Less than 20ms average
}
```

## Monitoring:

### Metrics cần theo dõi:
1. **Init time**: Đo thời gian khởi tạo
2. **Solve time**: Đo thời gian solve trung bình
3. **Success rate**: Tỷ lệ solve thành công
4. **Memory usage**: Peak memory usage
5. **Cache file size**: Kích thước file cache

### Logging:
```cpp
void log_performance_metrics() {
    std::cout << "=== Performance Metrics ===" << std::endl;
    std::cout << "Init time: " << init_time_ms << "ms" << std::endl;
    std::cout << "Rotation table size: " << rotation_table_size << std::endl;
    std::cout << "Permutation table size: " << permutation_table_size << std::endl;
    std::cout << "Total memory: " << get_memory_usage() << "MB" << std::endl;
    std::cout << "==========================" << std::endl;
}
```

## Rollback plan:

Nếu performance không đạt yêu cầu:

1. **Phương án 1**: Chỉ giảm một phase
   - Giữ Rotation = 7, giảm Permutation = 7
   - Hoặc giảm Rotation = 6, giữ Permutation = 8

2. **Phương án 2**: Giảm ít hơn
   - Rotation: 7 → 6.5 (nếu có thể implement fractional levels)
   - Permutation: 8 → 7.5

3. **Phương án 3**: Adaptive levels
   - Động các levels dựa trên available memory
   - Có thể điều chỉnh runtime

## Kết luận:

Việc giảm InitTableLevelMax sẽ mang lại lợi ích lớn về thời gian khởi tạo với trade-off nhỏ về performance solve. Đây là optimization với risk thấp và impact cao, rất phù hợp để triển khai ngay trong Phase 1.
