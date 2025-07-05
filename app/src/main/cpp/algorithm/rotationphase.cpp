#include "rotationphase.h"
#include "utils.h"

using namespace kube;
using namespace kube::rotation_phase;

uint16 EdgeStateList::DataNextMoveTable[EdgeStateList::StateCount][int(Move::Move_NB)] = {};
uint16 CornerStateList::DataNextMove[CornerStateList::StateCount][int(Move::Move_NB)] = {};
uint16 EdgePosList::DataNextMove[NumState][int(Move::Move_NB)] = {};
std::unordered_map<uint32, MutableMoveList> Cube::EndPhaseSearchTable = std::unordered_map<uint32, MutableMoveList>();


















