#pragma once
namespace kube {
#define dim_arr(x) (sizeof(x) / sizeof((x)[0]))

constexpr bool FastMode = false;

typedef signed char int8;
typedef unsigned char uint8;
typedef short int16;
typedef unsigned short uint16;
typedef int int32;
typedef unsigned int uint32;
typedef long long int64;
typedef unsigned long long uint64;
///
/// \brief The EdgePos enum describe position of each edge block
///
enum class EdgePos : uint8 {
    _UF, /**< Up front >**/
    _UR, /**< Up right >**/
    _UB, /**< Up back >**/
    _UL, /**< Up left >**/
    _FL, /**< Front left >**/
    _FR, /**< Front right >**/
    _BR, /**< Back right >**/
    _BL, /**< Back left >**/
    _DF, /**< Down front >**/
    _DR, /**< Down right >**/
    _DB, /**< Down back >**/
    _DL, /**< Down left >**/
    Count
};
constexpr int EdgeCount = static_cast<int>(EdgePos::Count);


///
/// \brief The CornerPos enum describe position of each corner block
///
enum class CornerPos : uint8 {
    _ULF, /**< Up left front >**/
    _URF, /**< Up right front >**/
    _URB, /**< Up right back >**/
    _ULB, /**< Up left back >**/
    _DLF, /**< Down left front >**/
    _DRF, /**< Down right front >**/
    _DRB, /**< Down right back >**/
    _DLB, /**< Down left back >**/
    Count
};
constexpr int CornerCount = static_cast<int>(CornerPos::Count);
constexpr CornerPos operator-(CornerPos pos, int val){
    return CornerPos(int(pos) - val);
}

///
/// \brief The EdgeState enum State normal or invert of each edge block
/// If block contain color U or D, it's state is normal if this color is upwards
/// or downwards. Or color is front or behind if this block on Layer-2
///
enum class EdgeState {
    NORMAL,
    INVERT
};
constexpr void flip_edge(EdgeState &state){
    state = static_cast<EdgeState>(1 - int(state));
}

///
/// \brief The CornerState enum describe state of corner block
///
enum class CornerState {
    NEUTRAL,  /**< Init state, with U or D color is upwards or downwards >**/
    POSITIVE, /**< Corner will become neutral if rotate it clockwise >**/
    NEGATIVE  /**< Corner will become neutral if rotate it anti-clockwise >**/
};
constexpr void rotate_corner_clockwise(CornerState &state){
    state = static_cast<CornerState>((int(state) + 2) % 3);
}
constexpr void rotate_corner_anticlockwise(CornerState &state){
    state = static_cast<CornerState>((int(state) + 1) % 3);
}
}







