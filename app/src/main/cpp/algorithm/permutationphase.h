#pragma once
#include "defines.h"
#include "utils.h"
#include "move.h"
#include "blockcube.h"
#include <unordered_map>
#include <iostream>
#include <fstream>
#include <iostream>
#include <set>
#include <vector>
#include <unordered_set>

namespace kube {
namespace permutation_phase {

// Vị trí hoán vị của các cạnh trên & dưới (8 viên)
struct UpDownEdgeStateList {
    enum {
        Amount = 8,
        NumState = Factorial<Amount>::Result // 40320
    };

    static uint16 DataNextMove[NumState][Move::NUM_NOT_ROTATE_MOVE];
    static void initLookupTable(){
        for (int i = 0; i < NumState; i++){
            for (Move move: Move::getNotRotate()){
                UpDownEdgeStateList test(static_cast<uint16>(i));
                test.doMove(move);
                DataNextMove[i][ShortenMoveList::moveToInt(move)] = test.data();
            }
        }
    }

    UpDownEdgeStateList(){}
    UpDownEdgeStateList(uint16 data): _data(data){}

    void doMove(Move move){
        auto p = permutation();
        auto newP = p;
        for (int i = 0; i < p.size(); i++){
            auto srcDes = i;
            if (srcDes >= 4) srcDes += 4;
            EdgePos des = EdgePos(srcDes);
            move.move_edge(des);
            int posDes = static_cast<int>(des);
            if (posDes > 4){
                posDes -= 4;
            }
            newP[posDes] = p[i];
        }
        setPermutation(newP);
    }

    void doLookupMove(Move move){
        _data = DataNextMove[_data][ShortenMoveList::moveToInt(move)];
    }

    void setPermutation(std::vector<EdgePos> edgePosList){
        // Chuyển đổi giá trị edge, từ 0-1-2-3-8-9-10-11 sang 0-1-2-3-4-5-6-7
        std::vector<int> edgeInts;
        for (auto edgePos: edgePosList){
            int v = static_cast<int>(edgePos);
            if (v < 4){
                edgeInts.push_back(v);
            } else {
                edgeInts.push_back(v - 4);
            }
        }
        _data = PermutationTranslation<Amount, uint16>::toPosition(edgeInts);
    }

    std::vector<EdgePos> permutation() const {
        auto edgeInts = PermutationTranslation<Amount, uint16>::fromPosition(_data);
        std::vector<EdgePos> out;
        for (auto edInt: edgeInts){
            if (edInt < 4){
                out.push_back(EdgePos(edInt));
            } else {
                out.push_back(EdgePos(edInt + 4));
            }
        }
        return out;
    }

    std::string debugString() const {
        auto p = permutation();
        std::string str;
        for (int i = 0; i < p.size(); i++){
            str.append(std::to_string(int(p.at(i)))).append(",");
        }
        return str;
    }

    void setData(uint16 data){ _data = data; }
    constexpr uint16 data() const { return _data; }
private:
    uint16 _data = 0;
};

///
/// \brief The CornerStateList struct lưu lại vị trí hoán vị của các viên góc
///
struct CornerStateList {
    enum {
        Amount = 8,
        NumState = Factorial<Amount>::Result // 40320
    };

    static uint16 DataNextMove[NumState][Move::NUM_NOT_ROTATE_MOVE];
    static void initLookupTable(){
        for (int i = 0; i < NumState; i++){
            for (Move move: Move::getNotRotate()){
                CornerStateList test(static_cast<uint16>(i));
                test.doMove(move);
                DataNextMove[i][ShortenMoveList::moveToInt(move)] = test.data();
            }
        }
    }

    CornerStateList(){}
    CornerStateList(uint16 data): _data(data){}

    void doMove(Move move){
        auto p = permutation();
        auto newP = p;
        for (int i = 0; i < p.size(); i++){
            CornerPos des = CornerPos(i);
            move.move_corner(des);
            newP[int(des)] = p[i];
        }
        setPermutation(newP);
    }

    void doLookupMove(Move move){
        _data = DataNextMove[_data][ShortenMoveList::moveToInt(move)];
    }

    void setPermutation(std::vector<CornerPos> edgePosList){
        // Chuyển đổi giá trị edge, từ 0-1-2-3-8-9-10-11 sang 0-1-2-3-4-5-6-7
        std::vector<int> edgeInts;
        for (auto edgePos: edgePosList){
            int v = static_cast<int>(edgePos);
            edgeInts.push_back(v);
        }
        _data = PermutationTranslation<Amount, uint16>::toPosition(edgeInts);
    }

    std::vector<CornerPos> permutation() const {
        auto edgeInts = PermutationTranslation<Amount, uint16>::fromPosition(_data);
        std::vector<CornerPos> out;
        for (auto edInt: edgeInts){
            out.push_back(CornerPos(edInt));
        }
        return out;
    }
    std::string debugString() const {
        auto p = permutation();
        std::string str;
        for (int i = 0; i < p.size(); i++){
            str.append(std::to_string(int(p.at(i)))).append(",");
        }
        return str;
    }
    void setData(uint16 data){ _data = data; }
    constexpr uint16 data() const { return _data; }
private:
    uint16 _data = 0;
};

struct MidEdgeStateList {
    enum {
        Amount = 4,
        NumState = Factorial<Amount>::Result // 24
    };

    static uint8 DataNextMove[NumState][Move::NUM_NOT_ROTATE_MOVE];
    static void initLookupTable(){
        for (int i = 0; i < NumState; i++){
            for (Move move: Move::getNotRotate()){
                MidEdgeStateList test(static_cast<uint8>(i));
                test.doMove(move);
                DataNextMove[i][ShortenMoveList::moveToInt(move)] = test.data();
            }
        }
    }

    MidEdgeStateList(){}
    MidEdgeStateList(uint8 data): _data(data){}

    void doMove(Move move){
        auto p = permutation();
        auto newP = p;
        for (int i = 0; i < p.size(); i++){
            EdgePos des = EdgePos(i + 4);
            move.move_edge(des);
//            auto des = edgeDoMove(EdgePos(i + 4), move);
            int posDes = static_cast<int>(des);
            posDes -= 4;
            newP[posDes] = p[i];
        }
        setPermutation(newP);
    }

    void doLookupMove(Move move){
        _data = DataNextMove[_data][ShortenMoveList::moveToInt(move)];
    }

    void setPermutation(std::vector<EdgePos> edgePosList){
        // Chuyển đổi giá trị edge, từ 0-1-2-3-8-9-10-11 sang 0-1-2-3-4-5-6-7
        std::vector<int> edgeInts;
        for (auto edgePos: edgePosList){
            int v = static_cast<int>(edgePos);
            edgeInts.push_back(v - 4);
        }
        _data = PermutationTranslation<Amount, uint8>::toPosition(edgeInts);
    }

    std::vector<EdgePos> permutation() const {
        auto edgeInts = PermutationTranslation<Amount, uint8>::fromPosition(_data);
        std::vector<EdgePos> out;
        for (auto edInt: edgeInts){
            out.push_back(EdgePos(edInt + 4));
        }
        return out;
    }
    std::string debugString() const {
        auto p = permutation();
        std::string str;
        for (int i = 0; i < p.size(); i++){
            str.append(std::to_string(int(p.at(i)))).append(",");
        }
        return str;
    }
    void setData(uint8 data){ _data = data; }
    constexpr uint8 data() const { return _data; }
private:
    uint8 _data = 0;
};

static std::unordered_map<uint64, MutableShortenMoveList> EndPhaseSearchTable;

// Cho phép tìm kiếm nhanh
static std::vector<char> fsb_1, fsb_2, fsb_3, fsb_4;
constexpr int se_1 = 15920414, se_2 = 11424437, se_3 = 15856074, se_4 = 18287995;
// 25920414, 21424437, 25856074, 28287995
struct CubeState {
    enum {
        InitTableLevelMax = 8,
        SearchLevelMax = 12
    };
    UpDownEdgeStateList updownEdges;
    MidEdgeStateList midEdges;
    CornerStateList corners;

    static void init_lookup_table(){
        UpDownEdgeStateList::initLookupTable();
        MidEdgeStateList::initLookupTable();
        CornerStateList::initLookupTable();

        CubeState cube0;
        EndPhaseSearchTable.clear();
        EndPhaseSearchTable[cube0.data()] = MutableShortenMoveList();

        std::vector<std::pair<uint64, MutableShortenMoveList>> dataEachLevels[InitTableLevelMax + 1];
        dataEachLevels[0].push_back(std::make_pair(cube0.data(), MutableShortenMoveList()));
        // 8 level: 2,753,039
        for (int level = 1; level <= InitTableLevelMax; level++){
            int prevLevel = level - 1;

            for (auto p: dataEachLevels[prevLevel]){
                auto prevCubeData = p.first;
                auto prevMoveList = p.second;
                for (auto move: Move::getNotRotate()){
                    if (prevMoveList.size() > 0
                            && prevMoveList.moveAt(prevMoveList.size() - 1).get_base() == Move(move).get_base()){
                        continue;
                    }
                    if (prevMoveList.size() > 1){
                        const auto &p_2 = prevMoveList.moveAt(prevMoveList.size() - 2);
                        const auto &p_1 = prevMoveList.moveAt(prevMoveList.size() - 1);
                        if (p_1 / 6 == move / 6 && p_2 / 3 == move / 3){
                            continue;
                        }
                    }
                    auto prevCube = CubeState::fromData(prevCubeData);
                    prevCube.doLookupMove(move);
                    MutableShortenMoveList newMoveList = prevMoveList;
                    newMoveList.prepend(Move(move).get_undo_move());
                    uint64 newCubeData = prevCube.data();
                    if (EndPhaseSearchTable.find(newCubeData) == EndPhaseSearchTable.end()){
                        EndPhaseSearchTable[newCubeData] = newMoveList;
                        dataEachLevels[level].push_back(std::make_pair(newCubeData, newMoveList));
                    }
                }
            }
        }

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

        std::cout << "End phase search permutation table size " << EndPhaseSearchTable.size() << std::endl;
    }

    static std::vector<Move> solve(const CubeState &cubeState, int levelLimit = SearchLevelMax + 1){


        auto data = cubeState.data();
        if (EndPhaseSearchTable.find(data) != EndPhaseSearchTable.end()){
            return EndPhaseSearchTable[data].toVector();
        }

        std::vector<std::pair<uint64, MutableShortenMoveList>> searchTableOnLevels[SearchLevelMax + 1];
        searchTableOnLevels[0].push_back(std::make_pair(cubeState.data(), MutableShortenMoveList()));

        const auto fsb_1Ptr = fsb_1.data();
        const auto fsb_2Ptr = fsb_2.data();
        const auto fsb_3Ptr = fsb_3.data();
        const auto fsb_4Ptr = fsb_4.data();

        auto time = steady_clock::now();
        if (levelLimit > SearchLevelMax + 1){
            levelLimit = SearchLevelMax + 1;
        }

        for (int level = 1; level < levelLimit; level++){
            int prevLevel = level - 1;
//            std::cout << "Test at level " << level << " count " << searchTableOnLevels[prevLevel].size()
//                      << " time " << duration_of<ms>(time, steady_clock::now()) << " ms" << std::endl;
            for (const auto &e: searchTableOnLevels[prevLevel]){
                const auto &prevCubeData = e.first;
                const auto &prevMoveList = e.second;
                for (const auto &move: Move::getNotRotate()){
                    if (prevMoveList.size() > 0
                            && (prevMoveList.moveAt(prevMoveList.size() - 1) / 3) == (move / 3)){
                        continue;
                    }
                    if (prevMoveList.size() > 1){
                        const auto &p_2 = prevMoveList.moveAt(prevMoveList.size() - 2);
                        const auto &p_1 = prevMoveList.moveAt(prevMoveList.size() - 1);
                        if (p_1 / 6 == move / 6 && p_2 / 3 == move / 3){
                            continue;
                        }
                    }
                    auto prevCube = CubeState::fromData(prevCubeData);
                    prevCube.doLookupMove(move);

                    MutableShortenMoveList newMoveList = prevMoveList;
                    newMoveList.add(move);
                    uint64 newCubeData = prevCube.data();

//                    bool isContainsInTable =
//                            fsb_1Ptr[newCubeData % se_1]
//                           && fsb_2Ptr[newCubeData % se_2]
//                            && fsb_3Ptr[newCubeData % se_3]
//                            && fsb_4Ptr[newCubeData % se_4]
//                            &&
//                            EndPhaseSearchTable.find(newCubeData) != EndPhaseSearchTable.end();
                    bool isContainsInTable = EndPhaseSearchTable.find(newCubeData) != EndPhaseSearchTable.end();
                    if (isContainsInTable){
                        std::vector<Move> result = newMoveList.toVector();
                        auto endPhaseMove = EndPhaseSearchTable[newCubeData].toVector();
                        result.insert(result.end(), endPhaseMove.begin(), endPhaseMove.end());
//                        std::cout << "endphase move" << endPhaseMove.size()  << ", " << endPermutationPhaseSearchTable[newCubeData].data()
//                                  << ", " << endPermutationPhaseSearchTable[newCubeData].size() << ", " << newCubeData << std::endl;
                        return result;
                    }
                    searchTableOnLevels[level].push_back(std::make_pair(newCubeData, newMoveList));
                }
            }
        }

        return std::vector<Move>();
    }

    void doLookupMove(Move move){
        updownEdges.doLookupMove(move);
        midEdges.doLookupMove(move);
        corners.doLookupMove(move);
    }

    static bool is_end_phase_file_available(){
        std::ifstream f("perm.dat");
        return f.good();
    }

    static void save_end_phase_to_file(){
        std::ofstream wf("perm.dat", std::ios::out | std::ios::binary);
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
    static std::unordered_map<uint64, MutableShortenMoveList> load_end_phase_from_file(){
        int file_size;
        {
            std::ifstream rf("perm.dat", std::ios::ate | std::ios::binary);
            file_size = rf.tellg();
            rf.close();
        }
        if (file_size == 0){
            std::cout << "endphase file empty";
            return {};
        }
        std::ifstream rf("perm.dat", std::ios::out | std::ios::binary);
        if (!rf){
            std::cout << "cannot open endphase file";
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
        return output;
    }

    uint64 data() const {
        return static_cast<uint64>(updownEdges.data())
                + (static_cast<uint64>(corners.data()) * UpDownEdgeStateList::NumState)
                + (static_cast<uint64>(midEdges.data()) * (UpDownEdgeStateList::NumState * CornerStateList::NumState));
    }
    static CubeState fromData(uint64 data){
        CubeState cube;
        cube.updownEdges.setData(data % UpDownEdgeStateList::NumState);
        cube.corners.setData((data / CornerStateList::NumState) % CornerStateList::NumState);
        cube.midEdges.setData(data / (UpDownEdgeStateList::NumState * CornerStateList::NumState));
        return cube;
    }
    static CubeState fromBlockCube(const BlockCube &blockCube){
        CubeState out;

        std::vector<EdgePos> edgePermutation;
        std::vector<EdgePos> edgeMidPermutation;
        std::vector<CornerPos> cornerPermutation;

        for (int i = 0; i < EdgeCount; i++){
            if (i < 4 || i >= 8){
                edgePermutation.push_back(blockCube.edges[i].pos);
            } else {
                edgeMidPermutation.push_back(blockCube.edges[i].pos);
            }
        }
        for (int i = 0; i < CornerCount; i++){
            cornerPermutation.push_back(blockCube.corners[i].pos);
        }

        out.updownEdges.setPermutation(edgePermutation);
        out.midEdges.setPermutation(edgeMidPermutation);
        out.corners.setPermutation(cornerPermutation);

        return out;
    }
};
}
}































