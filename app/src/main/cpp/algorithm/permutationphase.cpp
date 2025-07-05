#include "permutationphase.h"

using namespace kube;
using namespace kube::permutation_phase;
uint16 permutation_phase::UpDownEdgeStateList::DataNextMove[NumState][Move::NUM_NOT_ROTATE_MOVE] = {};
uint16 permutation_phase::CornerStateList::DataNextMove[NumState][Move::NUM_NOT_ROTATE_MOVE] = {};
uint8 permutation_phase::MidEdgeStateList::DataNextMove[NumState][Move::NUM_NOT_ROTATE_MOVE] = {};
std::unordered_map<uint64, MutableShortenMoveList> EndPhaseSearchTable = std::unordered_map<uint64, MutableShortenMoveList>();

std::vector<char> fsb_1 = {}, fsb_2 = {}, fsb_3 = {}, fsb_4 = {};
//QHash<uint64, MutableShortenMoveList> searchTable = {};
