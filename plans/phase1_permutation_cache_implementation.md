# Triển khai tối ưu hóa Phase 1: Thêm cache loading cho Permutation Phase

## File cần sửa đổi: permutationphase.h

### 1. Thêm cache loading vào init_lookup_table()

```cpp
static void init_lookup_table(){
    UpDownEdgeStateList::initLookupTable();
    MidEdgeStateList::initLookupTable();
    CornerStateList::initLookupTable();

    // THÊM CACHE LOADING
    if (is_end_phase_file_available()) {
        std::cout << "Loading permutation phase cache file..." << std::endl;
        EndPhaseSearchTable = load_end_phase_from_file();
        if (!EndPhaseSearchTable.empty()) {
            std::cout << "Loaded permutation phase cache: " << EndPhaseSearchTable.size() << " entries" << std::endl;
            
            // Vẫn cần init FSB arrays
            fsb_1.resize(se_1);
            fsb_2.resize(se_2);
            fsb_3.resize(se_3);
            fsb_4.resize(se_4);
            
            std::fill(fsb_1.begin(), fsb_1.end(), 0);
            std::fill(fsb_2.begin(), fsb_2.end(), 0);
            std::fill(fsb_3.begin(), fsb_3.end(), 0);
            std::fill(fsb_4.begin(), fsb_4.end(), 0);
            
            const auto fsb_1Ptr = fsb_1.data();
            const auto fsb_2Ptr = fsb_2.data();
            const auto fsb_3Ptr = fsb_3.data();
            const auto fsb_4Ptr = fsb_4.data();
            
            for (auto kv: EndPhaseSearchTable){
                auto k = kv.first;
                int test1 = k % se_1;
                int test2 = k % se_2;
                int test3 = k % se_3;
                int test4 = k % se_4;
                fsb_1Ptr[test1] = 1;
                fsb_2Ptr[test2] = 1;
                fsb_3Ptr[test3] = 1;
                fsb_4Ptr[test4] = 1;
            }
            
            std::cout << "Permutation phase cache loaded successfully!" << std::endl;
            return;
        }
    }

    // PHẦN CODE GỐC (nếu không có cache file)
    std::cout << "Computing permutation phase lookup table..." << std::endl;
    
    CubeState cube0;
    EndPhaseSearchTable.clear();
    EndPhaseSearchTable[cube0.data()] = MutableShortenMoveList();

    // ... phần code BFS như cũ ...
    
    // THÊM SAVE CACHE SAU KHI COMPUTE XONG
    save_end_phase_to_file();
}
```

### 2. Sửa hàm load_end_phase_from_file()

```cpp
static std::unordered_map<uint64, MutableShortenMoveList> load_end_phase_from_file(){
    int file_size;
    {
        std::ifstream rf("perm.dat", std::ios::ate | std::ios::binary);
        if (!rf.is_open()) {
            std::cout << "Cannot open permutation cache file" << std::endl;
            return {};
        }
        file_size = rf.tellg();
        rf.close();
    }
    if (file_size == 0){
        std::cout << "Permutation cache file empty" << std::endl;
        return {};
    }
    
    std::ifstream rf("perm.dat", std::ios::in | std::ios::binary); // FIX: ios::in instead of ios::out
    if (!rf){
        std::cout << "Cannot open permutation cache file for reading" << std::endl;
        return {};
    }
    
    std::unordered_map<uint64, MutableShortenMoveList> output;
    int num_element = file_size / (sizeof(uint64) * 2);
    
    for (int i = 0; i < num_element; i++){
        uint64 cube_data = 0, move_list = 0;
        rf.read(reinterpret_cast<char *>(&cube_data), sizeof(cube_data));
        rf.read(reinterpret_cast<char *>(&move_list), sizeof(move_list));
        
        MutableShortenMoveList move_list_s;
        move_list_s.setData(move_list);
        output[cube_data] = move_list_s;
    }
    
    rf.close();
    return output;
}
```

### 3. Thêm hàm is_end_phase_file_available()

```cpp
static bool is_end_phase_file_available(){
    std::ifstream f("perm.dat");
    bool good = f.good();
    f.close();
    return good;
}
```

### 4. Sửa hàm save_end_phase_to_file()

```cpp
static void save_end_phase_to_file(){
    std::cout << "Saving permutation phase cache..." << std::endl;
    
    std::ofstream wf("perm.dat", std::ios::out | std::ios::binary);
    if (!wf){
        std::cout << "Cannot open permutation cache file for writing" << std::endl;
        return;
    }
    
    for (const auto &e: EndPhaseSearchTable){
        auto cube_data = e.first;
        auto move_list = e.second.data();
        wf.write(reinterpret_cast<const char *>(&cube_data), sizeof(cube_data));
        wf.write(reinterpret_cast<const char *>(&move_list), sizeof(move_list));
    }
    
    wf.close();
    std::cout << "Permutation phase cache saved: " << EndPhaseSearchTable.size() << " entries" << std::endl;
}
```

## Kết quả kỳ vọng:

1. **Lần đầu tiên chạy**: Vẫn chậm như cũ, nhưng sẽ tạo file cache
2. **Các lần chạy tiếp theo**: Nhanh hơn rất nhiều (từ ~2-3 phút xuống ~10-20 giây)

## Lưu ý:

- File `perm.dat` sẽ có kích thước khoảng 20-40MB
- Cần đảm bảo app có permission write file
- Nên thêm version check cho cache file để tránh incompatibility khi update algorithm

## Testing:

```cpp
// Test cache loading
TEST(PermutationPhase, CacheLoading) {
    // Xóa file cache
    std::remove("perm.dat");
    
    // Lần đầu - tạo cache
    auto start1 = std::chrono::high_resolution_clock::now();
    permutation_phase::CubeState::init_lookup_table();
    auto end1 = std::chrono::high_resolution_clock::now();
    auto duration1 = std::chrono::duration_cast<std::chrono::milliseconds>(end1 - start1);
    
    // Lần thứ hai - load cache
    auto start2 = std::chrono::high_resolution_clock::now();
    permutation_phase::CubeState::init_lookup_table();
    auto end2 = std::chrono::high_resolution_clock::now();
    auto duration2 = std::chrono::duration_cast<std::chrono::milliseconds>(end2 - start2);
    
    // Cache loading phải nhanh hơn ít nhất 80%
    EXPECT_LT(duration2.count(), duration1.count() * 0.2);
}
```
