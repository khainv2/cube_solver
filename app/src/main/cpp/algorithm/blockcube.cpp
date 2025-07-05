#include "blockcube.h"
#include "move.h"
using namespace kube;
void BlockCube::do_move(Move move)
{
    auto pos_edge_affected = move.find_affected_edges();
    auto posCornerAffected = move.find_affected_corners();

    EdgePos epos0 = pos_edge_affected[0];
    EdgePos epos1 = pos_edge_affected[1];
    EdgePos epos2 = pos_edge_affected[2];
    EdgePos epos3 = pos_edge_affected[3];

    CornerPos cpos0 = posCornerAffected[0];
    CornerPos cpos1 = posCornerAffected[1];
    CornerPos cpos2 = posCornerAffected[2];
    CornerPos cpos3 = posCornerAffected[3];

    EdgeBlock block_edge_0 = edges[int(epos0)];
    EdgeBlock block_edge_1 = edges[int(epos1)];
    EdgeBlock block_edge_2 = edges[int(epos2)];
    EdgeBlock block_edge_3 = edges[int(epos3)];

    CornerBlock block_corner_0 = corners[int(cpos0)];
    CornerBlock block_corner_1 = corners[int(cpos1)];
    CornerBlock block_corner_2 = corners[int(cpos2)];
    CornerBlock block_corner_3 = corners[int(cpos3)];

    if (move.type() == Move::CLOCKWISE){
        kube::swap_ring(block_edge_0, block_edge_1, block_edge_2, block_edge_3);
        kube::swap_ring(block_corner_0, block_corner_1, block_corner_2, block_corner_3);
    } else if (move.type() == Move::ANTICLOCKWISE){
        kube::swap_ring(block_edge_3, block_edge_2, block_edge_1, block_edge_0);
        kube::swap_ring(block_corner_3, block_corner_2, block_corner_1, block_corner_0);
    } else {
        std::swap(block_edge_0, block_edge_2);
        std::swap(block_edge_1, block_edge_3);
        std::swap(block_corner_0, block_corner_2);
        std::swap(block_corner_1, block_corner_3);
    }

    if (move.can_flip_edge()){
        flip_edge(block_edge_0.state);
        flip_edge(block_edge_1.state);
        flip_edge(block_edge_2.state);
        flip_edge(block_edge_3.state);
    }

    if (move == Move::R || move == Move::R_ || move == Move::F || move == Move::F_){
        rotate_corner_anticlockwise(block_corner_0.state);
        rotate_corner_anticlockwise(block_corner_2.state);
        rotate_corner_clockwise(block_corner_1.state);
        rotate_corner_clockwise(block_corner_3.state);
    }
    if (move == Move::L || move == Move::L_ || move == Move::B || move == Move::B_){
        rotate_corner_clockwise(block_corner_0.state);
        rotate_corner_clockwise(block_corner_2.state);
        rotate_corner_anticlockwise(block_corner_1.state);
        rotate_corner_anticlockwise(block_corner_3.state);
    }

    edges[int(epos0)] = block_edge_0;
    edges[int(epos1)] = block_edge_1;
    edges[int(epos2)] = block_edge_2;
    edges[int(epos3)] = block_edge_3;

    corners[int(cpos0)] = block_corner_0;
    corners[int(cpos1)] = block_corner_1;
    corners[int(cpos2)] = block_corner_2;
    corners[int(cpos3)] = block_corner_3;
}

void BlockCube::do_list_move(std::vector<Move> moves)
{
    for (auto move: moves){
        do_move(move);
    }
}

#include <regex>
#include <sstream>
void BlockCube::do_list_move(std::string moves)
{
    std::regex_replace(moves, std::regex("  "), " ");
    std::string word;
    std::istringstream iss(moves, std::istringstream::in);

    while(iss >> word){
        auto move = Move::from_string(word);
        do_move(move);
    }
}
