#include "move.h"

using namespace kube;

EdgePos Move::MOVE_2_EDGE_LOOKUP_TABLE[Move::Move_NB / TYPE_NB][NUM_AFFECTED_BLOCK] = {
        { EdgePos::_UF, EdgePos::_UR, EdgePos::_UB, EdgePos::_UL },
        { EdgePos::_DF, EdgePos::_DL, EdgePos::_DB, EdgePos::_DR },
        { EdgePos::_FL, EdgePos::_UL, EdgePos::_BL, EdgePos::_DL },
        { EdgePos::_FR, EdgePos::_DR, EdgePos::_BR, EdgePos::_UR },
        { EdgePos::_UF, EdgePos::_FL, EdgePos::_DF, EdgePos::_FR },
        { EdgePos::_UB, EdgePos::_BR, EdgePos::_DB, EdgePos::_BL }
};

CornerPos Move::MOVE_2_CORNER_LOOKUP_TABLE[Move::Move_NB / TYPE_NB][NUM_AFFECTED_BLOCK] = {
        { CornerPos::_ULF, CornerPos::_URF, CornerPos::_URB, CornerPos::_ULB },
        { CornerPos::_DLF, CornerPos::_DLB, CornerPos::_DRB, CornerPos::_DRF },
        { CornerPos::_ULF, CornerPos::_ULB, CornerPos::_DLB, CornerPos::_DLF },
        { CornerPos::_URF, CornerPos::_DRF, CornerPos::_DRB, CornerPos::_URB },
        { CornerPos::_ULF, CornerPos::_DLF, CornerPos::_DRF, CornerPos::_URF },
        { CornerPos::_ULB, CornerPos::_URB, CornerPos::_DRB, CornerPos::_DLB }
};

Move::Value Move::ALL[] = {
    U, U_, U2, D, D_, D2,
    L, L_, L2, R, R_, R2,
    F, F_, F2, B, B_, B2,
};

Move::Value Move::NOT_ROTATE[] = {
    U, U_, U2,
    D, D_, D2,
    L2, R2, F2, B2
};


std::string Move::to_string() const {
    switch (_value){
    case U: return "U";
    case D: return "D";
    case L: return "L";
    case R: return "R";
    case F: return "F";
    case B: return "B";

    case U_: return "U'";
    case D_: return "D'";
    case L_: return "L'";
    case R_: return "R'";
    case F_: return "F'";
    case B_: return "B'";

    case U2: return "U2";
    case D2: return "D2";
    case L2: return "L2";
    case R2: return "R2";
    case F2: return "F2";
    case B2: return "B2";
    default: return "";
    }
}

Move Move::from_string(const std::string &m)
{
    if (m == "U") return U;
    else if (m == "D") return D;
    else if (m == "L") return L;
    else if (m == "R") return R;
    else if (m == "F") return F;
    else if (m == "B") return B;

    else if (m == "U'") return U_;
    else if (m == "D'") return D_;
    else if (m == "L'") return L_;
    else if (m == "R'") return R_;
    else if (m == "F'") return F_;
    else if (m == "B'") return B_;

    else if (m == "U2") return U2;
    else if (m == "D2") return D2;
    else if (m == "L2") return L2;
    else if (m == "R2") return R2;
    else if (m == "F2") return F2;
    else if (m == "B2") return B2;

    return U;
}
