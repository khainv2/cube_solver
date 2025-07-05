#pragma once
#include "rotationphase.h"
namespace kube {
void init();
std::vector<Move> solve(const BlockCube &blockCube);
std::vector<Move> solve(const std::string &stringMove);
}
