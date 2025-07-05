#include <QDebug>
#include <QApplication>
#include <QElapsedTimer>

#include "mainwindow.h"
#include "algorithm/defines.h"
#include "algorithm/rotationphase.h"
#include "cubedisplayer.h"
#include "algorithm/permutationphase.h"
#include "algorithm/solver.h"

using namespace kube;


int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    kube::init();

    int64 total_time = 0;
    for (int i = 0; i < 100; i++){
        auto time = steady_clock::now();
        std::string moveListStr;
        std::vector<Move> moveList;
        for (int j = 0; j < 25; j++){
            Move move;
            do {
                move = qrand() * Move::Move_NB / RAND_MAX;
            } while (j > 0 && move.get_base() == moveList.at(j - 1).get_base());
            moveList.push_back(move);
            moveListStr += move.to_string() + " ";
        }
        std::cout << "_______________________________________________________________________________" << std::endl;
        std::cout << "Generated move list " << moveListStr << std::endl;

//        std::string pattern = "U B' F L' U B' R D L F' R U2 L' U2 R2 D2 F R L U2 L' F' D L2 D";
        auto result = kube::solve(moveListStr);
        ColorfulCube initCube;
        initCube.parseFrom("rrrrrrrrr-bbbbbbbbb-wwwwwwwww-ggggggggg-yyyyyyyyy-ooooooooo");
//        std::cout << "str" << initCube.toString() << std::endl;
//        qDe
        BlockCube blockCube = initCube.toBlockCube();
        blockCube.do_list_move(moveListStr);
//        {
//            // Test
//            ColorfulCube cubeTest;
//            cubeTest.fromBlockCube(blockCube);
//            auto str = cubeTest.toString();
//            std::cout << "test" << cubeTest.debugString() << std::endl;
//            std::cout << "str" << str << std::endl;
//            ColorfulCube cubeTest2;
//            cubeTest2.parseFrom(str);
//            std::cout << "str2" << cubeTest2.toString() << std::endl;
//            std::cout << "IS EQUALLLLL" << (str == cubeTest2.toString()) << std::endl;
//        }
        blockCube.do_list_move(result);
        bool isResultValid = blockCube == initCube.toBlockCube();

        int64 time_calculated = duration_of<ms>(time, steady_clock::now());
        total_time += time_calculated;
        int64 avr_time = (total_time / (i + 1));
        std::cout << "Time for solver "
                  << time_calculated
                  << " ms, avr time = " << avr_time << " ms"
                  << std::endl;

        std::string result_str;
        for (int i = 0; i < result.size(); i++){
            result_str += result.at(i).to_string() + " ";
        }
        std::cout << "result " << (isResultValid ? " OK " : " ERRROR ") << ", size = " << result.size() << ", " << result_str << std::endl;
        if (!isResultValid){
            break;
        }
    }

    CubeDisplayer cubeDisplayer;
    cubeDisplayer.resize(800, 600);
//    cubeDisplayer.setCube(cube);
    cubeDisplayer.show();

    return a.exec();
}
