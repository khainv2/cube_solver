#pragma once

#include "utils.h"
#include "move.h"
#include "blockcube.h"

#include <set>
#include <bitset>
#include <vector>
#include <unordered_map>
#include <unordered_set>
#include <iostream>
#include <algorithm>

#include <fstream>
#include <iostream>

namespace kube {

///
/// \brief rotation_phase là pha đầu tiên để giải rubik, mục tiêu bao gồm:
/// - Đưa tất cả các cạnh về trạng thái 'True Flip' (tất cả các viên cạnh của rubik sẽ giữ trạng thái
/// 'True Flip' nếu chỉ xoay U, D, R, L, F2, B2 mà không xoay F-F'-B-B')
/// - Đưa tất cả các góc về trạng thái 'True Rotation' (tất cả các viên góc của rubik sẽ giữ trạng thái
/// 'True Flip' nếu xoay U, D, R2, L2, F2, B2)
/// - Đưa tất cả viên ở tầng giữa về tầng giữa (vị trí có thể bị hoán vị, các viên ở tầng giữa cũng không đổi với
/// các move U, D, R2, L2, F2, B2)
///
namespace rotation_phase {
///
/// \brief The EdgeStateList mô tả trạng thái của tất cả các viên cạnh của rubik
/// Toàn bộ trạng thái thuần túy được lưu trong 1 biến int16 duy nhất sử dụng các bit (do chỉ có 12 viên cạnh)
///
class EdgeStateList {
private:
    /**
     * @brief _data stores edge flipping values as bits
     */
    uint16 _data = 0;

public:
    ///
    /// \brief SpecialPos một đặc biệt của cạnh, do số lượng các cạnh ở trạng thái 'True Flip' luôn chẵn, trạng thái
    /// của viên cạnh cuối cùng có thể đoán được thông qua 11 trạng thái còn lại
    ///
    constexpr static EdgePos SpecialPos = EdgePos(11);

    ////
    /// \brief StateCount tổng số trạng thái có thể của các cạnh (2^11= 2048)
    ///
    constexpr static int StateCount = PowerOfTwo<EdgeCount - 1>::Result;

    ///
    /// \brief DataNextMoveTable bảng lưu lại toàn bộ trạng thái của cạnh khi thực hiện một bước xoay bất kỳ
    /// trong 18 bước xoay
    ///
    static uint16 DataNextMoveTable[StateCount][Move::Move_NB];

    ///
    /// \brief init_lookup_table thực hiện tính toán & lưu toàn bộ tham số cho bảng \param DataNextMoveTable
    ///
    static void init_lookup_table(){
        for (uint16 state = 0; state < StateCount; state++){
            for (Move move: Move::getAll()){
                EdgeStateList test(state);
                test.do_move(move);
                DataNextMoveTable[state][int(move)] = test._data;
            }
        }
    }

public:
    EdgeStateList(){}
    EdgeStateList(uint16 data): _data(data){}

    // Trả về thông tin trạng thái của cạnh ở vị trí bất kỳ
    EdgeState state_at(EdgePos pos) const {
        if (pos != SpecialPos){
            return EdgeState(Bitwise::isBitOn(_data, int(pos)));
        } else {
            return EdgeState(
                Bitwise::countSetBit(_data & (~(1 << int(SpecialPos)))) % 2
            );
        }
    }

    // Thiết lập trạng thái của một viên cạnh bất kỳ
    void set_state_at(EdgePos pos, EdgeState state){
        if (pos != SpecialPos){
            if (state == EdgeState::NORMAL){
                Bitwise::clearBit(_data, int(pos));
            } else {
                Bitwise::setBit(_data, int(pos));
            }
        }
    }

    ///
    /// \brief do_move thực hiện cập nhật trạng thái khi di chuyển một bước bất kỳ
    /// hàm chỉ sử dụng trong quá trình khởi tạo, khi thực sự tính toán sẽ chỉ dùng
    /// tra bảng qua hàm do_lookup_move.
    ///
    void do_move(Move move){
        // Tìm các vị trí bị ảnh hưởng bởi một bước di chuyển (ví dụ thực hiện R sẽ
        // có FR, BR, DR, UR bị thay đổi)
        auto pos_affected = move.find_affected_edges();
        EdgePos pos0 = pos_affected[0];
        EdgePos pos1 = pos_affected[1];
        EdgePos pos2 = pos_affected[2];
        EdgePos pos3 = pos_affected[3];

        // Lấy trạng thái của các cạnh trước khi bị di chuyển
        EdgeState state0 = state_at(pos0);
        EdgeState state1 = state_at(pos1);
        EdgeState state2 = state_at(pos2);
        EdgeState state3 = state_at(pos3);

        // Swap giá trị của các khối cạnh tùy thuộc dạng move là closkwise, anticlockwise hay 180 độ
        if (move.type() == Move::CLOCKWISE){
            kube::swap_ring(state0, state1, state2, state3);
        } else if (move.type() == Move::ANTICLOCKWISE){
            kube::swap_ring(state3, state2, state1, state0);
        } else {
            std::swap(state0, state2);
            std::swap(state1, state3);
        }

        // Thực hiện lật cạnh trong trường hợp bước di chuyển thuộc dạng F-F'-B-B'
        if (move.can_flip_edge()){
            flip_edge(state0);
            flip_edge(state1);
            flip_edge(state2);
            flip_edge(state3);
        }

        set_state_at(pos0, state0);
        set_state_at(pos1, state1);
        set_state_at(pos2, state2);
        set_state_at(pos3, state3);
    }

    ///
    /// \brief do_lookup_move thực hiện di chuyển sử dụng tra bảng
    ///
    inline void do_lookup_move(Move move){
        _data = DataNextMoveTable[_data][int(move)];
    }

    ///
    /// \brief debug_string Returns a short string for debugging
    ///
    std::string debug_string() const {
        std::string str;
        for (int i = 0; i < EdgeCount; i++){
            if (state_at(EdgePos(i)) == EdgeState::NORMAL){
                str.append("N-");
            } else {
                str.append("F-");
            }
        }
        return str;
    }

    ///
    /// \brief data trả về giá trị nhỏ hơn 2^11
    ///
    uint16 data() const { return _data; }

    ///
    /// \brief set_data chỉ thiết lập các giá trị nhỏ hơn 2^11
    ///
    void set_data(uint16 data){
        _data = data;
    }
};

///
/// \brief The EdgeStateList mô tả trạng thái của tất cả các góc
///
struct CornerStateList {
private:
    ///
    /// \brief _data lưu lại trạng thái tất cả các góc sử dụng hệ cơ số 3
    ///
    uint16 _data = 0;
public:
    ///
    /// \brief SpecialPos một đặc biệt của góc, do góc cuối cùng có thể được đoán thông qua các viên góc còn lại
    ///
    constexpr static CornerPos SpecialPos = CornerPos(7);

    ////
    /// \brief StateCount tổng số trạng thái có thể của các góc = 3^7 = 2187
    ///
    constexpr static int StateCount = PowerOfThree<CornerCount - 1>::Result;

    ///
    /// \brief DataNextMove lưu lại toàn bộ trạng thái của góc khi thực hiện một trong 18 bước xoay
    ///
    static uint16 DataNextMove[StateCount][int(Move::Move_NB)];

    ///
    /// \brief init_lookup_table hàm thực hiện tính toán & lưu toàn bộ tham số cho bảng \param DataNextMove
    ///
    static void init_lookup_table(){
        for (int i = 0; i < StateCount; i++){
            for (Move move: Move::getAll()){
                CornerStateList test(static_cast<uint16>(i));
                test.do_move(move);
                DataNextMove[i][int(move)] = test.data();
            }
        }
    }

    CornerStateList(){}
    CornerStateList(uint16 data): _data(data){}

    ///
    /// \brief state_at Hàm trả về trạng thái của các viên góc
    /// (vị trí đặc biệt được tính toán thông qua vị trí các viên còn lại)
    ///
    constexpr CornerState state_at(CornerPos pos) const {
        if (pos != CornerPos(SpecialPos)){
            return CornerState((_data / P3[int(pos)]) % 3);
        } else {
            int totalState = (((_data / PowerOfThree<0>::Result) % 3)
                            + ((_data / PowerOfThree<1>::Result) % 3)
                            + ((_data / PowerOfThree<2>::Result) % 3)
                            + ((_data / PowerOfThree<3>::Result) % 3)
                            + ((_data / PowerOfThree<4>::Result) % 3)
                            + ((_data / PowerOfThree<5>::Result) % 3)
                            + ((_data / PowerOfThree<6>::Result) % 3)) % 3;
            if (totalState == 0){
                return CornerState::NEUTRAL;
            } else {
                return CornerState(3 - totalState);
            }
        }
    }

    ///
    /// \brief set_state_at hàm thiết lập vị trí cho một viên góc bất kỳ
    ///
    constexpr void set_state_at(CornerPos pos, CornerState state){
        if (pos != CornerPos(SpecialPos)){
            auto current = uint32(state) * P3[int(pos)];
            auto bigger = (pos == SpecialPos - 1)
                    ? 0 : (_data / P3[int(pos) + 1] * P3[int(pos) + 1]);
            auto littler = (pos == CornerPos(0))
                    ? 0 : (_data % P3[int(pos)]);
            _data = uint16(current) + uint16(bigger) + uint16(littler);
        }
    }

    ///
    /// \brief do_move thực hiện cập nhật trạng thái khi di chuyển một bước bất kỳ
    /// hàm chỉ sử dụng trong quá trình khởi tạo, khi thực sự tính toán sẽ chỉ dùng
    /// tra bảng qua hàm do_lookup_move.
    ///
    void do_move(Move move){
        auto posAffected = move.find_affected_corners();

        // Tìm kiếm các viên góc bị ảnh hưởng
        CornerPos pos0 = posAffected[0];
        CornerPos pos1 = posAffected[1];
        CornerPos pos2 = posAffected[2];
        CornerPos pos3 = posAffected[3];

        // Lưu lại trạng thái các viên góc trước khi di chuyển
        CornerState state0 = state_at(pos0);
        CornerState state1 = state_at(pos1);
        CornerState state2 = state_at(pos2);
        CornerState state3 = state_at(pos3);

        // Thực hiện hoán vị các viên góc tùy thuộc theo dạng nước di chuyển
        if (move.type() == Move::CLOCKWISE){
            kube::swap_ring(state0, state1, state2, state3);
        } else if (move.type() == Move::ANTICLOCKWISE){
            kube::swap_ring(state3, state2, state1, state0);
        } else {
            std::swap(state0, state2);
            std::swap(state1, state3);
        }

        // Thực hiện lật góc đối với các move đặc biệt
        if (move == Move::R || move == Move::R_ || move == Move::F || move == Move::F_){
            rotate_corner_anticlockwise(state0);
            rotate_corner_anticlockwise(state2);
            rotate_corner_clockwise(state1);
            rotate_corner_clockwise(state3);
        }
        if (move == Move::L || move == Move::L_ || move == Move::B || move == Move::B_){
            rotate_corner_clockwise(state0);
            rotate_corner_clockwise(state2);
            rotate_corner_anticlockwise(state1);
            rotate_corner_anticlockwise(state3);
        }

        set_state_at(pos0, state0);
        set_state_at(pos1, state1);
        set_state_at(pos2, state2);
        set_state_at(pos3, state3);
    }

    ///
    /// \brief do_lookup_move thực hiện di chuyển sử dụng cơ chế tra bảng
    ///
    inline void do_lookup_move(Move move){
        _data = DataNextMove[_data][move];
    }

    std::string debug_string() const {
        std::string str;
        for (int i = 0; i < CornerCount; i++){
            switch (state_at(CornerPos(i))){
            case CornerState::NEUTRAL: str.append("N-"); break;
            case CornerState::NEGATIVE: str.append("E-"); break;
            case CornerState::POSITIVE: str.append("P-"); break;
            }
        }
        return str;
    }

    uint16 data() const {
        return _data;
    }

    void set_data(uint16 data){
        _data = data;
    }

};

///
/// \brief The EdgePosList struct mô tả vị trí của 4 viên cạnh (không quan tâm trạng thái lật) trong 12 vị trí
/// của rubik. Các viên cạnh này có thể hoán đổi tùy ý, vì thế số khả năng có thể có edge pos là Tổ hợp chập
/// 4 của 12 = 495 phần tử
///
///
///
struct EdgePosList {
private:
    ///
    /// \brief _data toàn bộ giá trị nhỏ hơn 495, thể hiện tổ hợp chập 4 của 12
    ///
    uint16 _data;
public:
    ///
    /// \brief NumMidEdges số lượng viên ở vị trí cạnh
    ///
    constexpr static int NumMidEdges = 4;

    ///
    /// \brief NumState số lượng trạng thái có thể (công thức tính theo tổ hợp chập 4 của 12 = 495)
    ///
    constexpr static int NumState = nchoosek(int(EdgePos::Count), NumMidEdges);

    ///
    /// \brief InitValue Giá trị ban đầu của EdgePosList, khi tất cả các viên giữa nằm đúng vị trí
    ///
    constexpr static uint16 InitValue = 0xf0;

    ///
    /// \brief DataNextMove Lưu lại toàn bộ trạng thái khi thực hiện di chuyển (sau để tiện tra bảng)
    ///
    static uint16 DataNextMove[NumState][int(Move::Move_NB)];
    static void init_lookup_table(){
        for (int i = 0; i < NumState; i++){
            for (Move move: Move::getAll()){
                EdgePosList test(static_cast<uint16>(i));
                test.do_move(move);
                DataNextMove[i][move] = test.data();
            }
        }
    }

    EdgePosList(){}
    EdgePosList(uint16 data): _data(data){}

    void set_state(uint16 bitPos){
        _data = combination_to_position(bitPos, EdgeCount);
    }

    uint16 getState() const {
        return position_to_combination(EdgeCount, NumMidEdges, _data);
    }

    void do_move(Move move){
        auto bitPosition = getState();
        EdgePos pos[NumMidEdges];
        int index = 0;
        for (int i = 0; i < EdgeCount; i++){
            if (Bitwise::isBitOn(bitPosition, i)){
                pos[index++] = EdgePos(i);
            }
        }

        uint16 newData = 0;
        for (int i = 0; i < NumMidEdges; i++){
            EdgePos next_pos = pos[i];
            move.move_edge(next_pos);
            Bitwise::setBit(newData, int(next_pos));
        }

        set_state(newData);
    }

    void do_lookup_move(Move move){
        _data = DataNextMove[_data][int(move)];
    }

    std::string debug_string() const {
        return std::bitset<16>(getState()).to_string();
    }

    void set_data(uint16 data){
        _data = data;
    }

    uint16 data() const {
        return _data;
    }
};

struct Cube {
    using CubeState = uint32;
    enum {
        InitTableLevelMax = 7, // 7 for solve faster, 6 for optimize memory
        SearchLevelMax = 6
    };
    EdgeStateList edge_states;
    CornerStateList corner_states;
    EdgePosList mid_edge_states;

    static std::unordered_map<CubeState, MutableMoveList> EndPhaseSearchTable;
    static void init_lookup_table(){

        EdgeStateList::init_lookup_table();
        CornerStateList::init_lookup_table();
        EdgePosList::init_lookup_table();

        if (is_end_phase_file_available()) {
            std::cout << "Start loading file...";
            load_end_phase_from_file(EndPhaseSearchTable);
            std::cout << "Load done search table size " << EndPhaseSearchTable.size() << std::endl;
            return;
        }

        Cube cube_0 = create_default_state();
        EndPhaseSearchTable.clear();
        EndPhaseSearchTable[cube_0.data()] = MutableMoveList();

        struct Node {
            CubeState state;
            MutableMoveList move_list;
            Node(){}
            Node(CubeState state, MutableMoveList move_list): state(state), move_list(move_list){}
        };

        std::vector<Node> data_each_levels[InitTableLevelMax + 1];

        data_each_levels[0].push_back(Node(cube_0.data(), MutableMoveList()));
        int reserveSize = 1;
        static const auto all_moves = Move::getAll();
        const int moveCount = all_moves.size();
        for (int level = 1; level <= InitTableLevelMax; level++){
            reserveSize *= moveCount;
            data_each_levels[level].reserve(reserveSize);
        }

        // Tiền tính các nước đi hợp lệ
        std::vector<std::vector<std::vector<Move>>> allowed_moves(moveCount + 1, std::vector<std::vector<Move>>(moveCount + 1));

        // Hàm lấy chỉ số của Move
        auto get_move_index = [](Move move) -> int {
            if (move == Move::Move_NB) {
                return 0;
            } else {
                return static_cast<int>(move) + 1; // Giả sử Move bắt đầu từ 0
            }
        };

        // Tiền tính danh sách nước đi hợp lệ cho từng cặp prev_2nd_move và prev_move
        for (int prev_2nd_move_idx = 0; prev_2nd_move_idx <= moveCount; prev_2nd_move_idx++) {
            Move prev_2nd_move = (prev_2nd_move_idx == 0) ? Move::Move_NB : all_moves[prev_2nd_move_idx - 1];
            for (int prev_move_idx = 0; prev_move_idx <= moveCount; prev_move_idx++) {
                Move prev_move = (prev_move_idx == 0) ? Move::Move_NB : all_moves[prev_move_idx - 1];
                for (auto move : all_moves) {
                    if (prev_move == Move::Move_NB || !prev_move.is_same_base(move)) {
                        if (prev_2nd_move == Move::Move_NB ||
                            !(prev_move.is_same_base_or_opposite(move) && prev_2nd_move.is_same_base(move))) {
                            allowed_moves[prev_2nd_move_idx][prev_move_idx].push_back(move);
                        }
                    }
                }
            }
        }

        // Vòng lặp chính
        for (int level = 1; level <= InitTableLevelMax; level++) {
            int prev_level = level - 1;
            for (const auto& p : data_each_levels[prev_level]) {
                auto prev_cube_data = p.state;
                auto prev_move_list = p.move_list;
                Move prev_move = (level > 1) ? prev_move_list.moveAt(prev_level - 1) : Move::Move_NB;
                Move prev_2nd_move = (level > 2) ? prev_move_list.moveAt(prev_level - 2) : Move::Move_NB;

                int prev_2nd_move_idx = get_move_index(prev_2nd_move);
                int prev_move_idx = get_move_index(prev_move);

                // Sử dụng danh sách nước đi tiền tính
                for (auto move : allowed_moves[prev_2nd_move_idx][prev_move_idx]) {
                    auto prev_cube = Cube::from_data(prev_cube_data);
                    prev_cube.do_lookup_move(move);

                    MutableMoveList new_move_list = prev_move_list;
                    new_move_list.add(move);
                    CubeState new_cube_data = prev_cube.data();
                    if (EndPhaseSearchTable.find(new_cube_data) == EndPhaseSearchTable.end()) {
                        EndPhaseSearchTable[new_cube_data] = new_move_list;
                        if (level < InitTableLevelMax) {
                            data_each_levels[level].push_back(Node(new_cube_data, new_move_list));
                        }
                    }
                }
            }
        }
        std::cout << "Rotation phase search table size " << EndPhaseSearchTable.size() << std::endl;
        save_end_phase_to_file();
    }

    static bool is_end_phase_file_available(){
        std::ifstream f("rot.dat");
        return f.good();
    }

    static void save_end_phase_to_file(){
        std::ofstream wf("rot.dat", std::ios::out | std::ios::binary);
        if (!wf){
            std::cout << "Cannot open file" << std::endl;
            return;
        }
        for (const auto &e: EndPhaseSearchTable){
            auto cube_data = e.first;
            auto move_list = e.second.data();
            wf.write(reinterpret_cast<const char *>(&cube_data), sizeof(cube_data));
            wf.write(reinterpret_cast<const char *>(&move_list), sizeof(move_list));
        }
    }

    static void load_end_phase_from_file(std::unordered_map<CubeState, MutableMoveList> &output){
        int file_size;
        {
            std::ifstream rf("rot.dat", std::ios::ate | std::ios::binary);
            file_size = rf.tellg();
            rf.close();
        }
        if (file_size == 0){
            std::cout << "endphase file empty";
            return;
        }
        std::ifstream rf("rot.dat", std::ios::out | std::ios::binary);
        if (!rf){
            std::cout << "cannot open endphase file";
            return;
        }
        int num_element = file_size / (sizeof(uint64) * 2);
        for (int i = 0; i < num_element; i++){
            CubeState cube_data = 0;
            MutableMoveList move_list;
            rf.read(reinterpret_cast<char *>(&cube_data), sizeof(cube_data));
            rf.read(reinterpret_cast<char *>(&move_list), sizeof(move_list));
            output[cube_data] = move_list;
        }
    }


    static std::vector<Move> solve(const Cube &cube_state){
        auto data = cube_state.data();
        if (EndPhaseSearchTable.find(data) != EndPhaseSearchTable.end()){
            return EndPhaseSearchTable[data].to_vector();
        }

        std::unordered_map<CubeState, MutableMoveList> search_table_on_levels[SearchLevelMax + 1];
        search_table_on_levels[0][cube_state.data()] = MutableMoveList();

        for (int level = 1; level <= SearchLevelMax; level++){
            int prevLevel = level - 1;
            for (auto e: search_table_on_levels[prevLevel]){
                auto prev_cube_data = e.first;
                auto prev_move_list = e.second;
                for (auto move: Move::getAll()){
                    if (prev_move_list.size() > 0
                            && prev_move_list.moveAt(prevLevel - 1).get_base() == Move(move).get_base()){
                        continue;
                    }
                    auto prev_cube = Cube::from_data(prev_cube_data);
                    prev_cube.do_lookup_move(move);


                    MutableMoveList new_move_list = prev_move_list;
                    new_move_list.add(move);
                    CubeState new_cube_data = prev_cube.data();
                    if (EndPhaseSearchTable.find(new_cube_data) != EndPhaseSearchTable.end()){
                        auto end_phase_moves = EndPhaseSearchTable[new_cube_data].to_reverse_vector();
                        std::vector<Move> result = new_move_list.to_vector();
                        result.insert(result.end(), end_phase_moves.begin(), end_phase_moves.end());
                        std::cout << "Rotation phase result " << result.size() << std::endl;
                        return result;
                    }
                    search_table_on_levels[level][new_cube_data] = new_move_list;
                }
            }
        }
        return std::vector<Move>();
    }

    static std::vector<std::vector<Move>> solveMulti(const Cube &cube_state, int deltaLimit = 2){
        auto data = cube_state.data();


        std::unordered_map<CubeState, MutableMoveList> search_table_on_levels[SearchLevelMax + 1];
        search_table_on_levels[0][cube_state.data()] = MutableMoveList();

        std::vector<std::vector<Move>> availableResult;
        int levelLimit = SearchLevelMax;
        if (EndPhaseSearchTable.find(data) != EndPhaseSearchTable.end()){
            availableResult.push_back(EndPhaseSearchTable[data].to_reverse_vector());
            levelLimit = deltaLimit;
        }
        for (int level = 1; level <= levelLimit; level++){
            int prevLevel = level - 1;
            for (auto e: search_table_on_levels[prevLevel]){
                auto prev_cube_data = e.first;
                auto prev_move_list = e.second;
                for (auto move: Move::getAll()){
                    if (prev_move_list.size() > 0
                            && prev_move_list.moveAt(prevLevel - 1).get_base() == Move(move).get_base()){
                        continue;
                    }
                    auto prev_cube = Cube::from_data(prev_cube_data);
                    prev_cube.do_lookup_move(move);

                    MutableMoveList new_move_list = prev_move_list;
                    new_move_list.add(move);
                    CubeState new_cube_data = prev_cube.data();
                    if (EndPhaseSearchTable.find(new_cube_data) != EndPhaseSearchTable.end()){
                        auto end_phase_moves = EndPhaseSearchTable[new_cube_data].to_reverse_vector();
                        std::vector<Move> result = new_move_list.to_vector();
                        result.insert(result.end(), end_phase_moves.begin(), end_phase_moves.end());
//                        std::cout << "Rotation phase result " << result.size() << std::endl;
                        if (level + deltaLimit < levelLimit){
                            levelLimit = level + deltaLimit;
                        }
                        availableResult.push_back(result);
                        if (availableResult.size() > 1000){
                            return availableResult;
                        }
                    }
                    if (level < levelLimit)
                        search_table_on_levels[level][new_cube_data] = new_move_list;
                }
            }
        }
        return availableResult;
    }



    void do_lookup_move(Move move){
        edge_states.do_lookup_move(move);
        corner_states.do_lookup_move(move);
        mid_edge_states.do_lookup_move(move);
    }

    CubeState data() const {
        return edge_states.data() * CornerStateList::StateCount * EdgePosList::NumState
                + corner_states.data() * EdgePosList::NumState
                + mid_edge_states.data();
    }

    static Cube from_data(CubeState data){
        Cube state;
        state.edge_states.set_data(data / (CornerStateList::StateCount * EdgePosList::NumState));
        state.corner_states.set_data((data % (CornerStateList::StateCount * EdgePosList::NumState))
                                  / EdgePosList::NumState);
        state.mid_edge_states.set_data(data % EdgePosList::NumState);
        return state;
    }

    static Cube create_default_state(){
        Cube out;
        out.edge_states.set_data(0);
        out.corner_states.set_data(0);
        out.mid_edge_states.set_state(EdgePosList::InitValue);
        return out;
    }

    static Cube from_block_cube(const BlockCube &cube){
        Cube out;
        for (int i = 0; i < EdgeCount; i++){
            out.edge_states.set_state_at(EdgePos(i), cube.edges[i].state);
        }

        for (int i = 0; i < CornerCount; i++){
            out.corner_states.set_state_at(CornerPos(i), cube.corners[i].state);
        }

        std::set<EdgePos> midEdges = {
            EdgePos::_FL,
            EdgePos::_FR,
            EdgePos::_BL,
            EdgePos::_BR
        };
        uint16 edge_bit = 0;
        for (int i = 0; i < EdgeCount; i++){
            if (midEdges.count(cube.edges[i].pos))
                Bitwise::setBit(edge_bit, i);
        }
        out.mid_edge_states.set_state(edge_bit);
        return out;
    }
};
}
}








