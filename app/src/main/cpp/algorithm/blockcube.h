#pragma once
#include "defines.h"
#include "move.h"
#include <vector>
namespace kube {
///
/// \brief The EdgeBlock struct describe real-state & position of edge block
///
struct EdgeBlock {
    EdgePos pos;
    EdgeState state;
    bool operator!=(const EdgeBlock &o) const {
        return pos != o.pos || state != o.state;
    }
};
///
/// \brief The CornerBlock struct describe real-state & position of corner block
///
struct CornerBlock {
    CornerPos pos;
    CornerState state;
    bool operator!=(const CornerBlock &o) const {
        return pos != o.pos || state != o.state;
    }
};
///
/// \brief The BlockCube struct describe a cube
///
struct BlockCube {
    EdgeBlock edges[EdgeCount];
    CornerBlock corners[CornerCount];

    void do_move(Move move);
    void do_list_move(std::vector<Move> moves);
    void do_list_move(std::string move_strings);

    bool operator==(const BlockCube &o) const {
        for (int i = 0; i < EdgeCount; i++)
            if (edges[i] != o.edges[i])
                return false;
        for (int i = 0; i < CornerCount; i++)
            if (corners[i] != o.corners[i])
                return false;
        return true;
    }
};
}
