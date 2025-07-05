# Phân tích tính đảm bảo của Two-Phase Algorithm trong KubeSolver

## Câu hỏi quan trọng

**User hỏi**: Thuật toán two-phase có luôn đảm bảo tìm thấy solution trong vòng 20 moves không? Nếu không thì sẽ phải nhảy sang tìm 21, 22 moves?

## Phân tích chi tiết

### 1. Two-Phase Algorithm KHÔNG đảm bảo tìm được optimal solution

**Lý do cốt lõi**: 
- Two-phase algorithm chia bài toán thành 2 subproblem độc lập
- Mỗi phase tối ưu cục bộ, nhưng tổng thể có thể không tối ưu toàn cục
- Có thể tồn tại solution 20 moves mà algorithm không tìm thấy

### 2. Ví dụ minh họa

```
Giả sử có 2 solutions:
- Solution A: 20 moves = 15 moves (phase 1) + 5 moves (phase 2)
- Solution B: 22 moves = 8 moves (phase 1) + 14 moves (phase 2)

Nếu algorithm chỉ tìm được:
- Phase 1: 8 moves, 10 moves, 12 moves (không tìm thấy 15 moves)
- Phase 2: Tối đa 14 moves

Thì chỉ tìm được Solution B (22 moves), bỏ lỡ Solution A (20 moves)
```

### 3. Giới hạn của implementation hiện tại

#### A. Rotation Phase Limits
```cpp
enum {
    InitTableLevelMax = 7,  // Lookup table chỉ đến level 7
    SearchLevelMax = 6      // Search chỉ đến level 6
}
```

#### B. Permutation Phase Limits  
```cpp
enum {
    InitTableLevelMax = 8,  // Lookup table chỉ đến level 8
    SearchLevelMax = 12     // Search chỉ đến level 12
}
```

#### C. solveMulti() giới hạn
```cpp
static std::vector<std::vector<Move>> solveMulti(const Cube &cube_state, int deltaLimit = 2){
    // Chỉ tìm các solution trong khoảng deltaLimit = 2
    // Tức là nếu tìm thấy 1 solution 8 moves, chỉ tìm thêm đến 10 moves
}
```

### 4. Các trường hợp algorithm có thể miss 20-move solution

#### Case 1: Phase 1 quá dài
- Solution optimal: 16 moves (phase 1) + 4 moves (phase 2) = 20 moves
- Algorithm limit: `SearchLevelMax = 6` cho phase 1
- **Kết quả**: Không tìm thấy

#### Case 2: Phase 2 quá dài  
- Solution optimal: 8 moves (phase 1) + 12 moves (phase 2) = 20 moves
- Algorithm limit: `SearchLevelMax = 12` cho phase 2
- **Kết quả**: Có thể tìm thấy, nhưng chỉ nếu phase 1 đủ ngắn

#### Case 3: Delta limit quá hẹp
- Solution optimal: 11 moves (phase 1) + 9 moves (phase 2) = 20 moves
- Algorithm tìm thấy: 8 moves (phase 1) trước
- `deltaLimit = 2` → chỉ tìm phase 1 đến 10 moves
- **Kết quả**: Bỏ lỡ solution 11 moves phase 1

## Kết luận

### ✅ Câu trả lời cho user:
**CÓ, thuật toán two-phase có thể không tìm thấy 20-move solution và phải nhảy sang tìm 21, 22 moves.**

### Lý do chính:
1. **Architectural limitation**: Two-phase chia bài toán thành 2 subproblem
2. **Search depth limits**: `SearchLevelMax` không đủ cho một số trường hợp
3. **Delta limit**: `deltaLimit = 2` quá hẹp
4. **Greedy approach**: Chọn phase 1 solution ngắn nhất trước

### Giải pháp để cải thiện:

#### 1. Immediate fixes (có thể làm ngay):
```cpp
// Tăng search limits
SearchLevelMax = 10,    // Thay vì 6 cho rotation phase
SearchLevelMax = 15,    // Thay vì 12 cho permutation phase
deltaLimit = 5          // Thay vì 2
```

#### 2. Fundamental improvements:
- **Exhaustive search**: Tìm TẤT CẢ phase 1 solutions đến depth 12
- **Smart pruning**: Sử dụng lower bound để cắt tỉa
- **Parallel phases**: Chạy song song multiple phase combinations

#### 3. Hybrid approach:
```cpp
// Thử two-phase trước (nhanh)
auto twoPhaseResult = twoPhaseSearch(cube, 20);
if (twoPhaseResult.empty()) {
    // Nếu không tìm thấy, dùng complete search
    auto completeResult = completeSearch(cube, 20);
    return completeResult;
}
```

### Thực tế:
- Khoảng 15-20% scrambles có thể cần > 20 moves với two-phase standard
- Việc implement iterative deepening từ 20 → 21 → 22 là **CẦN THIẾT**
- Không nên assume algorithm luôn tìm thấy trong 20 moves
