#include "solver.h"
#include "permutationphase.h"
#include "rotationphase.h"
#include "blockcube.h"
#include "colorfulcube.h"

using namespace kube;
void kube::init()
{
    auto time = steady_clock::now();
    rotation_phase::Cube::init_lookup_table();
    std::cout << "Time init rotation phase "
              << duration_of<ms>(time, steady_clock::now())
              << " ms" << std::endl;

    time = steady_clock::now();

    permutation_phase::CubeState::init_lookup_table();

    std::cout << "Time init permutation phase "
              << duration_of<ms>(time, steady_clock::now())
              << " ms" << std::endl;
}

std::vector<Move> kube::solve(const BlockCube &cube)
{
    rotation_phase::Cube cube_r = rotation_phase::Cube::from_block_cube(cube);
    auto time = steady_clock::now();
    auto allMoveListRot = rotation_phase::Cube::solveMulti(cube_r);

    std::cout << "Found move list rotation " << allMoveListRot.size() << std::endl;
    std::cout << "Time for solve rotation phase "
              << duration_of<ms>(time, steady_clock::now())
              << " ms" << std::endl;

    if (allMoveListRot.empty()){
        std::cout << "Cannot find rotation phase" << std::endl;
        return {};
    }

    int minimumResultLen = 99;
    std::vector<Move> minimumResult;

    for (const auto &moveListRot: allMoveListRot){

        BlockCube cube1 = cube;
        cube1.do_list_move(moveListRot);
        auto cube_p = permutation_phase::CubeState::fromBlockCube(cube1);
        int permMoveLimit = minimumResultLen - moveListRot.size() - permutation_phase::CubeState::InitTableLevelMax;
        if (permMoveLimit <= 0){
            break;
        }
//        std::cout << "Find move in " << moveListRot.size() << "Max " << minimumResultLen << std::endl;
        auto moveListPerm = permutation_phase::CubeState::solve(cube_p, permMoveLimit);
        if (moveListPerm.empty())
            continue;
        if (moveListPerm.size() + moveListRot.size() < minimumResultLen){
            minimumResultLen = moveListPerm.size() + moveListRot.size();

            std::vector<Move> output;
            output.insert(output.end(), moveListRot.begin(), moveListRot.end());
            output.insert(output.end(), moveListPerm.begin(), moveListPerm.end());
            minimumResult = output;
        }

        {
            std::string result_str;
            for (int i = 0; i < moveListRot.size(); i++){
                result_str += moveListRot.at(i).to_string() + " ";
            }
            std::cout << "  -Move list rot " << moveListRot.size() << " - " << result_str << std::endl;
        }
        {
            std::string result_str;
            for (int i = 0; i < moveListPerm.size(); i++){
                result_str += moveListPerm.at(i).to_string() + " ";
            }
            std::cout << "  -Move list perm " << moveListPerm.size() << " - " << result_str << std::endl;
        }
//        std::cout << "Test move list rot " << moveListRot.size() << ", perm " << moveListPerm.size() << ", total " << (moveListRot.size() + moveListPerm.size()) << std::endl;

    }
    std::cout << "Minimum result at" << minimumResultLen;
    std::cout << "Time for solve all "
              << duration_of<ms>(time, steady_clock::now())
              << " ms" << std::endl;
    return minimumResult;

}

std::vector<Move> kube::solve(const std::string &stringMove)
{
    ColorfulCube cube;
    cube.parseFrom("rrrrrrrrr-bbbbbbbbb-wwwwwwwww-ggggggggg-yyyyyyyyy-ooooooooo");
    BlockCube blockCube = cube.toBlockCube();
    blockCube.do_list_move(stringMove);
    return solve(blockCube);
}
