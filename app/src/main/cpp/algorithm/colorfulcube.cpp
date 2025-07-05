#include "colorfulcube.h"
#include "utils.h"

#include <map>
#include <sstream>
#include <algorithm>
#include <cctype>

#define R Color::RED
#define B Color::BLUE
#define W Color::WHITE
#define G Color::GREEN
#define Y Color::YELLOW
#define O Color::ORANGE

#define TL TilePos::TOP_LEFT
#define TM TilePos::TOP_MID
#define TR TilePos::TOP_RIGHT
#define ML TilePos::MID_LEFT
#define MM TilePos::MID_MID
#define MR TilePos::MID_RIGHT
#define BL TilePos::BOT_LEFT
#define BM TilePos::BOT_MID
#define BR TilePos::BOT_RIGHT

#define c(color, tile) color2Char(colors[int(color)][int(tile)])
std::string ColorfulCube::debugString() const
{
    auto color2Char = [](Color color) -> char {
        switch (color){
        case RED: return 'r';
        case BLUE: return 'b';
        case WHITE: return 'w';
        case GREEN: return 'g';
        case YELLOW: return 'y';
        case ORANGE: return 'o';
        default: return '0';
        }
    };

    char arr[54] = {
        c(R, TL),c(R, TM),c(R, TR)
       ,c(R, ML),c(R, MM),c(R, MR)
       ,c(R, BL),c(R, BM),c(R, BR)
       ,c(B, TL),c(B, TM),c(B, TR)
       ,c(W, TL),c(W, TM),c(W, TR)
       ,c(G, TL),c(G, TM),c(G, TR)
       ,c(Y, TL),c(Y, TM),c(Y, TR)
       ,c(B, ML),c(B, MM),c(B, MR)
       ,c(W, ML),c(W, MM),c(W, MR)
       ,c(G, ML),c(G, MM),c(G, MR)
       ,c(Y, ML),c(Y, MM),c(Y, MR)
       ,c(B, BL),c(B, BM),c(B, BR)
       ,c(W, BL),c(W, BM),c(W, BR)
       ,c(G, BL),c(G, BM),c(G, BR)
       ,c(Y, BL),c(Y, BM),c(Y, BR)
       ,c(O, TL),c(O, TM),c(O, TR)
       ,c(O, ML),c(O, MM),c(O, MR)
       ,c(O, BL),c(O, BM),c(O, BR)
    };
    auto ret =  std::string("\
                   +-----+-----+-----+\n\
                   |     |     |     |\n\
                   |  %1  |  %2  |  %3  |\n\
                   |     |     |     |\n\
                   +-----------------+\n\
                   |     |     |     |\n\
                   |  %4  |  %5  |  %6  |\n\
                   |     |     |     |\n\
                   +-----------------+\n\
                   |     |     |     |\n\
                   |  %7  |  %8  |  %9  |\n\
                   |     |     |     |\n\
 +-----+-----+-----------------------------+-----+-----+-----+-----+-----+\n\
 |     |     |     |     |     |     |     |     |     |     |     |     |\n\
 |  %10  |  %11  |  %12  |  %13  |  %14  |  %15  |  %16  |  %17  |  %18  |  %19  |  %20  |  %21  |\n\
 |     |     |     |     |     |     |     |     |     |     |     |     |\n\
 +-----------------------------------------------------------------------+\n\
 |     |     |     |     |     |     |     |     |     |     |     |     |\n\
 |  %22  |  %23  |  %24  |  %25  |  %26  |  %27  |  %28  |  %29  |  %30  |  %31  |  %32  |  %33  |\n\
 |     |     |     |     |     |     |     |     |     |     |     |     |\n\
 +-----------------------------------------------------------------------+\n\
 |     |     |     |     |     |     |     |     |     |     |     |     |\n\
 |  %34  |  %35  |  %36  |  %37  |  %38  |  %39  |  %40  |  %41  |  %42  |  %43  |  %44  |  %45  |\n\
 |     |     |     |     |     |     |     |     |     |     |     |     |\n\
 +-----+-----+-----------------------------+-----+-----+-----+-----+-----+\n\
                   |     |     |     |\n\
                   |  %46  |  %47  |  %48  |\n\
                   |     |     |     |\n\
                   +-----------------+\n\
                   |     |     |     |\n\
                   |  %49  |  %50  |  %51  |\n\
                   |     |     |     |\n\
                   +-----------------+\n\
                   |     |     |     |\n\
                   |  %52  |  %53  |  %54  |\n\
                   |     |     |     |\n\
                   +-----+-----+-----+");
    for (int i = 0; i < 54; i++){
        auto w = std::string("%") + std::to_string(i + 1);
        replace(ret, w, std::to_string(arr[i]));
    }
    return ret;
}
#undef c

#include <unordered_map>

ColorfulCube ColorfulCube::fromBlockCube(const BlockCube &blockCube)
{
    ColorfulCube d;
    std::map<EdgePos, std::vector<Color>> edge2Colors = {
        { EdgePos::_UF, {R, W} },
        { EdgePos::_UR, {R, G} },
        { EdgePos::_UB, {R, Y} },
        { EdgePos::_UL, {R, B} },
        { EdgePos::_FL, {W, B} },
        { EdgePos::_FR, {W, G} },
        { EdgePos::_BR, {Y, G} },
        { EdgePos::_BL, {Y, B} },
        { EdgePos::_DF, {O, W} },
        { EdgePos::_DR, {O, G} },
        { EdgePos::_DB, {O, Y} },
        { EdgePos::_DL, {O, B} },
    };
    std::map<EdgePos, std::vector<TilePos>> edge2Tiles = {
        { EdgePos::_UF, {BM, TM} },
        { EdgePos::_UR, {MR, TM} },
        { EdgePos::_UB, {TM, TM} },
        { EdgePos::_UL, {ML, TM} },
        { EdgePos::_FL, {ML, MR} },
        { EdgePos::_FR, {MR, ML} },
        { EdgePos::_BR, {ML, MR} },
        { EdgePos::_BL, {MR, ML} },
        { EdgePos::_DF, {TM, BM} },
        { EdgePos::_DR, {MR, BM} },
        { EdgePos::_DB, {BM, BM} },
        { EdgePos::_DL, {ML, BM} },
    };
    std::map<CornerPos, std::vector<Color>> corner2Colors = {
        { CornerPos::_ULF, {R, B, W} },
        { CornerPos::_URF, {R, W, G} },
        { CornerPos::_URB, {R, G, Y} },
        { CornerPos::_ULB, {R, Y, B} },
        { CornerPos::_DLF, {O, W, B} },
        { CornerPos::_DRF, {O, G, W} },
        { CornerPos::_DRB, {O, Y, G} },
        { CornerPos::_DLB, {O, B, Y} },
    };
    std::map<CornerPos, std::vector<TilePos>> corner2Tiles = {
        { CornerPos::_ULF, {BL, TR, TL} },
        { CornerPos::_URF, {BR, TR, TL} },
        { CornerPos::_URB, {TR, TR, TL} },
        { CornerPos::_ULB, {TL, TR, TL} },
        { CornerPos::_DLF, {TL, BL, BR} },
        { CornerPos::_DRF, {TR, BL, BR} },
        { CornerPos::_DRB, {BR, BL, BR} },
        { CornerPos::_DLB, {BL, BL, BR} },
    };

    for (int i = 0; i < Color::COLOR_NB; i++){
        d.colors[i][4] = Color(i);
    }

    for (int i = 0; i < EdgeCount; i++){
        auto edge = blockCube.edges[i];
        auto oriPos = EdgePos(edge.pos);
        auto curPos = EdgePos(i);
        // Lấy màu nguồn của block
        std::vector<Color> colorBlock = edge2Colors[oriPos];
        std::vector<Color> colorFaces = edge2Colors[curPos];
        // Lấy vị trí hiện tại của block
        auto tile = edge2Tiles[curPos];
        if (edge.state == EdgeState::INVERT){
            d.colors[int(colorFaces[0])][int(tile[0])] = colorBlock[1];
            d.colors[int(colorFaces[1])][int(tile[1])] = colorBlock[0];
        } else {
            d.colors[int(colorFaces[0])][int(tile[0])] = colorBlock[0];
            d.colors[int(colorFaces[1])][int(tile[1])] = colorBlock[1];
        }
    }

    for (int i  = 0; i < CornerCount; i++){
        auto corner = blockCube.corners[i];
        auto oriPos = CornerPos(corner.pos);
        auto curPos = CornerPos(i);

        auto colorBlock = corner2Colors[oriPos];
        auto colorFaces = corner2Colors[curPos];
        std::vector<TilePos> tiles = corner2Tiles[curPos];

        auto t1 = tiles[0];
        auto t2 = tiles[1];
        auto t3 = tiles[2];

        switch (corner.state){
        case CornerState::NEUTRAL:
            d.colors[int(colorFaces.at(0))][int(t1)] = colorBlock.at(0);
            d.colors[int(colorFaces.at(1))][int(t2)] = colorBlock.at(1);
            d.colors[int(colorFaces.at(2))][int(t3)] = colorBlock.at(2);
            break;
        case CornerState::POSITIVE:
            d.colors[int(colorFaces.at(0))][int(t1)] = colorBlock.at(2);
            d.colors[int(colorFaces.at(1))][int(t2)] = colorBlock.at(0);
            d.colors[int(colorFaces.at(2))][int(t3)] = colorBlock.at(1);
            break;
        case CornerState::NEGATIVE:
            d.colors[int(colorFaces.at(0))][int(t1)] = colorBlock.at(1);
            d.colors[int(colorFaces.at(1))][int(t2)] = colorBlock.at(2);
            d.colors[int(colorFaces.at(2))][int(t3)] = colorBlock.at(0);
            break;
        }
    }

    return d;
}
BlockCube ColorfulCube::toBlockCube() const
{
    auto color2Edge = [](std::vector<Color> colors) -> EdgePos {
        if (vt_contains(colors, R) && vt_contains(colors, W)) return EdgePos::_UF;
        if (vt_contains(colors, R) && vt_contains(colors, G)) return EdgePos::_UR;
        if (vt_contains(colors, R) && vt_contains(colors, Y)) return EdgePos::_UB;
        if (vt_contains(colors, R) && vt_contains(colors, B)) return EdgePos::_UL;

        if (vt_contains(colors, W) && vt_contains(colors, B)) return EdgePos::_FL;
        if (vt_contains(colors, W) && vt_contains(colors, G)) return EdgePos::_FR;
        if (vt_contains(colors, Y) && vt_contains(colors, B)) return EdgePos::_BL;
        if (vt_contains(colors, Y) && vt_contains(colors, G)) return EdgePos::_BR;

        if (vt_contains(colors, O) && vt_contains(colors, W)) return EdgePos::_DF;
        if (vt_contains(colors, O) && vt_contains(colors, G)) return EdgePos::_DR;
        if (vt_contains(colors, O) && vt_contains(colors, Y)) return EdgePos::_DB;
        if (vt_contains(colors, O) && vt_contains(colors, B)) return EdgePos::_DL;

        return EdgePos::_UL;
    };

    auto colorToState = [](std::vector<Color> colors) -> EdgeState {
        if (vt_contains(colors, R)) return vt_find_index(colors, R) > 0 ? EdgeState::INVERT : EdgeState::NORMAL;
        if (vt_contains(colors, O)) return vt_find_index(colors, O) > 0 ? EdgeState::INVERT : EdgeState::NORMAL;
        if (vt_contains(colors, G)) return vt_find_index(colors, G) > 0 ? EdgeState::NORMAL : EdgeState::INVERT;
        if (vt_contains(colors, B)) return vt_find_index(colors, B) > 0 ? EdgeState::NORMAL : EdgeState::INVERT;
        return EdgeState::NORMAL;
    };

    auto color2Corner = [](std::vector<Color> colors) -> CornerPos {
        if (vt_contains(colors, R) && vt_contains(colors, B) && vt_contains(colors, W)) return CornerPos::_ULF;
        if (vt_contains(colors, R) && vt_contains(colors, G) && vt_contains(colors, W)) return CornerPos::_URF;
        if (vt_contains(colors, R) && vt_contains(colors, G) && vt_contains(colors, Y)) return CornerPos::_URB;
        if (vt_contains(colors, R) && vt_contains(colors, B) && vt_contains(colors, Y)) return CornerPos::_ULB;

        if (vt_contains(colors, O) && vt_contains(colors, B) && vt_contains(colors, W)) return CornerPos::_DLF;
        if (vt_contains(colors, O) && vt_contains(colors, G) && vt_contains(colors, W)) return CornerPos::_DRF;
        if (vt_contains(colors, O) && vt_contains(colors, G) && vt_contains(colors, Y)) return CornerPos::_DRB;
        if (vt_contains(colors, O) && vt_contains(colors, B) && vt_contains(colors, Y)) return CornerPos::_DLB;

        return CornerPos::_ULB;
    };

    auto color2Rotate = [](std::vector<Color> colors) -> CornerState {
        if (vt_contains(colors, R)) return CornerState(vt_find_index(colors, R));
        if (vt_contains(colors, O)) return CornerState(vt_find_index(colors, O));
        return CornerState::NEUTRAL;
    };

    BlockCube blockCube;
    auto makeEdge = [this, &blockCube, &color2Edge, &colorToState](int index,
            Color face1, TilePos tile1, Color face2, TilePos tile2){
        std::vector<Color> colors = { this->colors[int(face1)][int(tile1)],
                                this->colors[int(face2)][int(tile2)] };
        EdgeBlock edge;
        edge.pos = color2Edge(colors);
        edge.state = colorToState(colors);
        blockCube.edges[index] = edge;
    };

    auto makeCorner = [this, &blockCube, &color2Corner, &color2Rotate](int index,
            Color face1, TilePos tile1, Color face2, TilePos tile2, Color face3, TilePos tile3){
        std::vector<Color> colors{
            this->colors[int(face1)][int(tile1)],
            this->colors[int(face2)][int(tile2)],
            this->colors[int(face3)][int(tile3)],
        };
        CornerBlock corner;
        corner.pos = color2Corner(colors);
        corner.state = color2Rotate(colors);
        blockCube.corners[index] = corner;
    };

    makeEdge(0, R, BM, W, TM);
    makeEdge(1, R, MR, G, TM);
    makeEdge(2, R, TM, Y, TM);
    makeEdge(3, R, ML, B, TM);

    makeEdge(4, W, ML, B, MR);
    makeEdge(5, W, MR, G, ML);
    makeEdge(6, Y, ML, G, MR);
    makeEdge(7, Y, MR, B, ML);

    makeEdge(8, O, TM, W, BM);
    makeEdge(9, O, MR, G, BM);
    makeEdge(10, O, BM, Y, BM);
    makeEdge(11, O, ML, B, BM);

    makeCorner(0, R, BL, B, TR, W, TL);
    makeCorner(1, R, BR, W, TR, G, TL);
    makeCorner(2, R, TR, G, TR, Y, TL);
    makeCorner(3, R, TL, Y, TR, B, TL);

    makeCorner(4, O, TL, W, BL, B, BR);
    makeCorner(5, O, TR, G, BL, W, BR);
    makeCorner(6, O, BR, Y, BL, G, BR);
    makeCorner(7, O, BL, B, BL, Y, BR);
    return blockCube;
}
bool ColorfulCube::parseFrom(const std::string &text)
{
    auto input = text;
    std::transform(input.begin(), input.end(), input.begin(),
        [](unsigned char c){ return std::tolower(c); });
    auto words = split(input, "-");
    if (words.size() != 6){
        return false;
    }

    std::map<char, Color> color_map = {
        { 'r', Color::RED },
        { 'b', Color::BLUE },
        { 'w', Color::WHITE },
        { 'g', Color::GREEN },
        { 'y', Color::YELLOW },
        { 'o', Color::ORANGE },
    };

    std::map<Color, int> countColors;

    for (int i = 0; i < words.size(); i++){
        auto word = words.at(i);
        if (word.size() != 9){
            return false;
        }

        for (int j = 0; j < 9; j++){
            auto ch = word[j];

            if (color_map.find(ch) == color_map.end()){
                return false;
            }

            // Kiểm tra giá trị center
            auto color = color_map[ch];
            if (j == 4 && Color(i) != color){
                return false;
            }

            countColors[color]++;
            colors[i][j] = color;
        }
    }
    return true;
}

#undef R
#undef B
#undef W
#undef G
#undef Y
#undef O
