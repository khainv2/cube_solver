#pragma once
#include "defines.h"
#include "blockcube.h"
namespace kube {
/**
 - Khối rubik được sử dụng dạng tiêu chuẩn, trong đó mặt F (front) có màu trắng,
mặt T (top) có màu đỏ và mặt L (left) có màu xanh dương
 - Vị trí các mặt trên khối rubik, trải ra dưới dạng 2D
                 +---------+
                 |   Red   |
        +----------------------------+-----------+
        |  Blue  |  White  |  Green  |  Yellow   |
        +----------------------------+-----------+
                 | Orange  |
                 +---------+
 - Chỉ số trên mỗi mặt
                  +-----+-----+-----+
                  |  0  |  1  |  2  |
                  +-----------------+
                  |  3  |  4  |  5  |
                  +-----------------+
                  |  6  |  7  |  8  |
+-----+-----+-----------------------------+-----+-----+-----+-----+-----+
|  0  |  1  |  2  |  0  |  1  |  2  |  0  |  1  |  2  |  0  |  1  |  2  |
+-----------------------------------------------------------------------+
|  3  |  4  |  5  |  3  |  4  |  5  |  3  |  4  |  5  |  3  |  4  |  5  |
+-----------------------------------------------------------------------+
|  6  |  7  |  8  |  6  |  7  |  8  |  6  |  7  |  8  |  6  |  7  |  8  |
+-----+-----+-----------------------------+-----+-----+-----+-----+-----+
                  |  0  |  1  |  2  |
                  +-----------------+
                  |  3  |  4  |  5  |
                  +-----------------+
                  |  6  |  7  |  8  |
                  +-----+-----+-----+
*/

/// Mô tả khối rubik dưới dạng nhiều màu sắc, được thể hiện bởi 9x6=54 màu khác
/// nhau
struct ColorfulCube {

    enum Color {
        RED, BLUE,
        WHITE, GREEN,
        YELLOW, ORANGE,
        COLOR_NB
    };

    /// Vị trí của các màu trên mỗi mặt
    enum TilePos {
        TOP_LEFT, TOP_MID, TOP_RIGHT,
        MID_LEFT, MID_MID, MID_RIGHT,
        BOT_LEFT, BOT_MID, BOT_RIGHT,
        TilePos_NB
    };

    /// Toàn bộ thông tin về màu sắc của khối cube
    Color colors[Color::COLOR_NB][TilePos_NB];

    static ColorfulCube fromBlockCube(const BlockCube &blockCube);

    BlockCube toBlockCube() const;

    // Tạo thành cube từ chuỗi, có dạng
    // 'rrrrrrrrr-bbbbbbbbb-wwwwwwwww-ggggggggg-yyyyyyyyy-ooooooooo' khi
    // hoàn chỉnh
    bool parseFrom(const std::string &text);

    // In ra giá trị từng từng màu của cục rubik
    std::string debugString() const;
};
}

using namespace kube;


