# Kế hoạch tối ưu hóa thuật toán KubeSolver để đạt God's Number (20 moves)

## Phân tích vấn đề hiện tại

### 1. Hiện trạng
- Solver hiện tại chỉ tìm được giải pháp 20-24 nước
- God's Number là 20 nước (đã được chứng minh)
- Thuật toán two-phase hiện tại chưa được tối ưu hóa tối đa

### 2. Các điểm yếu trong implementation hiện tại

#### A. Khởi tạo không tối ưu
```cpp
int minimumResultLen = 99; // Quá lớn, nên bắt đầu với 20
```

#### B. Công thức permMoveLimit chưa chính xác
```cpp
int permMoveLimit = minimumResultLen - moveListRot.size() - permutation_phase::CubeState::InitTableLevelMax;
```
- Công thức này trừ đi `InitTableLevelMax` (=8) một cách không cần thiết
- Gây ra việc cắt tỉa quá sớm

#### C. Thiếu Iterative Deepening
- Không sử dụng iterative deepening để đảm bảo tìm được giải pháp tối ưu
- Duyệt qua tất cả phase 1 solutions mà không sắp xếp theo độ dài

#### D. Lookup Table Levels chưa tối ưu
- Rotation Phase: `InitTableLevelMax = 7`
- Permutation Phase: `InitTableLevelMax = 8`
- Có thể cần tăng để cải thiện pruning

## Kế hoạch tối ưu hóa

### Phase 1: Tối ưu hóa cơ bản (1-2 ngày)

#### 1.1 Cải thiện solver logic
- [ ] Thay đổi `minimumResultLen` từ 99 xuống 20
- [ ] Sửa công thức `permMoveLimit` để không trừ `InitTableLevelMax`
- [ ] Implement iterative deepening search
- [ ] Sắp xếp phase 1 solutions theo độ dài

#### 1.2 Tối ưu hóa search parameters
- [ ] Thử nghiệm tăng `SearchLevelMax` trong rotation phase
- [ ] Thử nghiệm tăng `SearchLevelMax` trong permutation phase
- [ ] Điều chỉnh `InitTableLevelMax` để balance giữa memory và performance

### Phase 2: Tối ưu hóa nâng cao (3-5 ngày)

#### 2.1 Cải thiện heuristics
- [ ] Implement better lower bound estimation
- [ ] Thêm advanced pruning techniques
- [ ] Cải thiện move ordering

#### 2.2 Parallel processing
- [ ] Xử lý song song multiple phase 1 solutions
- [ ] Implement concurrent search trong mỗi phase

#### 2.3 Memory optimization
- [ ] Implement more efficient lookup table storage
- [ ] Sử dụng compressed representation
- [ ] Cache management cho better performance

### Phase 3: Thuật toán nâng cao (1-2 tuần)

#### 3.1 Hybrid approach
- [ ] Kết hợp two-phase với other algorithms (A*, IDA*)
- [ ] Implement bidirectional search
- [ ] Pattern database optimization

#### 3.2 Machine learning integration
- [ ] Sử dụng ML để improve move ordering
- [ ] Neural network heuristics
- [ ] Reinforcement learning approach

## Implementation Plan

### Tuần 1: Quick Wins
```cpp
// File: solver.cpp - Immediate improvements
std::vector<Move> kube::solve(const BlockCube &cube) {
    rotation_phase::Cube cube_r = rotation_phase::Cube::from_block_cube(cube);
    
    // IMPROVEMENT 1: Iterative deepening từ 1 đến 20
    for (int maxMoves = 1; maxMoves <= 20; maxMoves++) {
        auto allMoveListRot = rotation_phase::Cube::solveMultiWithLimit(cube_r, maxMoves);
        
        // IMPROVEMENT 2: Sắp xếp theo độ dài
        std::sort(allMoveListRot.begin(), allMoveListRot.end(), 
                  [](const auto& a, const auto& b) { return a.size() < b.size(); });
        
        for (const auto &moveListRot: allMoveListRot) {
            BlockCube cube1 = cube;
            cube1.do_list_move(moveListRot);
            auto cube_p = permutation_phase::CubeState::fromBlockCube(cube1);
            
            // IMPROVEMENT 3: Sửa công thức permMoveLimit
            int permMoveLimit = maxMoves - moveListRot.size();
            if (permMoveLimit <= 0) continue;
            
            auto moveListPerm = permutation_phase::CubeState::solve(cube_p, permMoveLimit);
            if (!moveListPerm.empty()) {
                // Found optimal solution
                std::vector<Move> result;
                result.insert(result.end(), moveListRot.begin(), moveListRot.end());
                result.insert(result.end(), moveListPerm.begin(), moveListPerm.end());
                return result;
            }
        }
    }
    return {};
}
```

### Tuần 2: Advanced Optimizations
1. **Tăng SearchLevelMax**
   - Rotation Phase: 6 → 8
   - Permutation Phase: 12 → 15

2. **Implement bidirectional search**
   - Search từ cả solved state và scrambled state
   - Meet in the middle approach

3. **Better pruning**
   - Implement IDA* với better heuristics
   - Pattern database cho corner và edge orientation

### Tuần 3-4: Performance Tuning
1. **Profiling và optimization**
   - Measure performance của từng component
   - Optimize memory allocation
   - Vectorization và SIMD optimization

2. **Parallel processing**
   - Multi-threading cho phase 1 solutions
   - Concurrent search strategies

## Expected Results

### Phase 1 (1-2 ngày):
- Giảm trung bình từ 22-24 moves xuống 20-22 moves
- Tăng tỷ lệ tìm được 20 moves từ 10% lên 30%

### Phase 2 (1 tuần):
- Đạt được 20 moves trong 60-80% trường hợp
- Giảm thời gian solve trung bình 20-30%

### Phase 3 (2-4 tuần):
- Đạt được 20 moves trong 90%+ trường hợp
- Thời gian solve < 100ms cho most cases

## Risk Assessment

### Low Risk (Phase 1):
- Sửa solver logic: Dễ implement, high impact
- Iterative deepening: Proven technique
- Search parameter tuning: Reversible changes

### Medium Risk (Phase 2):
- Parallel processing: Có thể có race conditions
- Memory optimization: Có thể affect stability
- Advanced heuristics: Cần testing kỹ lưỡng

### High Risk (Phase 3):
- ML integration: Phức tạp, cần dataset lớn
- Bidirectional search: Significant architecture changes
- Algorithm replacement: Có thể break existing functionality

## Conclusion

Kế hoạch này sẽ giúp KubeSolver tiến gần hơn đến God's Number thông qua:
1. **Immediate wins** với code fixes đơn giản
2. **Algorithm improvements** với advanced techniques
3. **Performance optimization** với parallel processing

Ưu tiên cao nhất là Phase 1 implementations vì chúng có risk thấp và impact cao. Sau đó mới tiến đến các optimizations phức tạp hơn.
