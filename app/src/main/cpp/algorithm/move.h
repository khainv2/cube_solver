 #pragma once
#include "defines.h"
#include "utils.h"
namespace kube {
///
/// \brief The Move struct describe how rubik turn by 6 face and 3 type
///
struct Move {
    enum Value : uint8 {
        U, U_, U2, D, D_, D2,
        L, L_, L2, R, R_, R2,
        F, F_, F2, B, B_, B2,
        Move_NB
    };
    enum Type {
        CLOCKWISE,
        ANTICLOCKWISE,
        ROT180,
        TYPE_NB
    };
    enum {
        // Number of block affected by each move (4 edge & 4 corner)
        NUM_AFFECTED_BLOCK = 4,
        NUM_NOT_ROTATE_MOVE = 10,
    };

    // Get all list move
    static Value ALL[];

    static std::vector<Value> getAll() {
        return {
                U, U_, U2, D, D_, D2,
                L, L_, L2, R, R_, R2,
                F, F_, F2, B, B_, B2,
        };
    }

    // Move do not change rotation of all edges & corners
    static Value NOT_ROTATE[];



    static std::vector<Value> getNotRotate() {
        return {
            U, U_, U2,
            D, D_, D2,
            L2, R2, F2, B2
        };
    }

    // Translate move to list edge pos affected
    static EdgePos MOVE_2_EDGE_LOOKUP_TABLE[Move::Move_NB / TYPE_NB][NUM_AFFECTED_BLOCK];

    // Translate move to list corner pos affected
    static CornerPos MOVE_2_CORNER_LOOKUP_TABLE[Move::Move_NB / TYPE_NB][NUM_AFFECTED_BLOCK];

    // Constructor
    Move(){}
    Move(Value value): _value(value){}
    Move(uint8 t): _value(static_cast<Value>(t)){}
    operator int() const  noexcept { return _value; }
    Move operator+(Value v){
        return Move(v);
    }

    // For debug
    std::string to_string() const;
    static Move from_string(const std::string &m);

    // For more function
    Type type() const  noexcept { return Type(_value % 3); }

    ///
    /// \brief get_base get base move (eg U' -> U)
    ///
    Move get_base() const noexcept { return Move(_value / 3 * 3); }

    bool is_same_base(const Move &another) const noexcept {
        return( _value / 3) == (another._value / 3);
    }

    ///
    /// \brief get_undo_move revert move (eg U' -> U, U' -> U', U2 -> U2)
    ///
    Move get_undo_move() const noexcept { return Move(((19 - _value) % 3) + (_value / 3 * 3)); }

    ///
    /// \brief can_flip_edge return true if
    ///
    bool can_flip_edge() const noexcept {
        return (_value == F || _value == F_
                || _value == B || _value == B_);
    }

    ///
    /// \brief move_edge move edge to another position
    ///
    void move_edge(EdgePos &pos) const noexcept {
        auto row = MOVE_2_EDGE_LOOKUP_TABLE[_value / TYPE_NB];
        int index = find_index(row, NUM_AFFECTED_BLOCK, pos);
        auto t = type();
        if (index >= 0){
            pos = row[(index + t + 3 * (t == CLOCKWISE)) % 4];
        }
    }

    ///
    /// \brief move_corner move corner to another position
    ///
    void move_corner(CornerPos &pos) const {
        auto row = MOVE_2_CORNER_LOOKUP_TABLE[_value / TYPE_NB];
        int index = find_index(row, NUM_AFFECTED_BLOCK, pos);
        auto t = type();
        if (index >= 0){
            pos = row[(index + t + 3 * (t == CLOCKWISE)) % 4];
        }
    }

    const EdgePos *find_affected_edges() const {
        return MOVE_2_EDGE_LOOKUP_TABLE[_value / TYPE_NB];
    }

    const CornerPos *find_affected_corners() const {
        return MOVE_2_CORNER_LOOKUP_TABLE[_value / TYPE_NB];
    }

    ///
    /// \brief is_opposite return true if 2 move is opposite or same base
    /// eg: U & D, L' & R2,..
    ///
    bool is_same_base_or_opposite(const Move &another){
        return (_value / 6) == (another / 6);
    }

private:
    ///
    /// \brief _value real value of move
    ///
    Value _value;
};





struct MoveList {
    enum : uint32 {
        MaxLengthSupported = 7,
        AllNullMove = 893871738
    };
    MoveList(){}

    Move moveAt(int index) const {
        return Move((_data / P19[index]) % 19);
    }

    void setMoveAt(int index, Move move){
        uint32 current = uint32(move) * P19[index];
        uint32 bigger = (index == MaxLengthSupported - 1)
                ? 0 : (_data / P19[index + 1] * P19[index + 1]);
        uint32 littler = (index == 0)
                ? 0 : (_data % P19[index]);
        _data = current + bigger + littler;
    }

    std::string debugString() const {
        std::string str;
        for (int i = 0; i < MaxLengthSupported; i++){
            auto move = moveAt(i);
            str.append(move.to_string());
            str.append(",");
        }
        return str;
    }

    void setData(uint32 data){
        _data = data;
    }
    uint32 data() const { return _data; }
private:
    uint32 _data = AllNullMove;
};

struct MutableMoveList : public MoveList {
    int size() const {
        for (int i = 0; i < MaxLengthSupported; i++){
            if (moveAt(i) == Move::Move_NB){
                return i;
            }
        }
        return MaxLengthSupported;
    }
    void prepend(Move move){
        int s = size();
        if (s < MaxLengthSupported){
            for (int i = MaxLengthSupported - 1; i > 0; i--){
                setMoveAt(i, moveAt(i - 1));
            }
            setMoveAt(0, move);
        }
    }
    void add(Move move){
        int s = size();
        if (s < MaxLengthSupported){
            setMoveAt(s, move);
        }
    }

    std::vector<Move> to_reverse_vector() const {
        std::vector<Move> out;
        for (int i = 0; i < size(); i++){
            out.insert(out.begin(), moveAt(i).get_undo_move());
        }
        return out;
    }

    std::vector<Move> to_vector() const {
        std::vector<Move> out;
        for (int i = 0; i < size(); i++){
            out.push_back(moveAt(i));
        }
        return out;
    }
};

struct ShortenMoveList {
    enum : uint64 {
        MaxLengthSupported = 18,
        AllNullMove = 5559917313492231480LL
    };
    ShortenMoveList(){}

    Move moveAt(uint32 index) const {
        return intToMove((_data / P11[index]) % 11);
    }

    void setMoveAt(uint32 index, Move move){
        uint64 intMove = moveToInt(move);
        uint64 current = intMove * P11[index];
        uint64 bigger = (index == MaxLengthSupported - 1)
                ? 0 : (_data / P11[index + 1] * P11[index + 1]);
        uint64 littler = (index == 0)
                ? 0 : (_data % P11[index]);
        _data = current + bigger + littler;
    }

    std::string debugString() const {
        std::string str;
        for (uint32 i = 0; i < MaxLengthSupported; i++){
            auto move = moveAt(i);
            str.append(move.to_string());
            str.append(",");
        }
        return str;
    }

    void setData(uint64 data){
        _data = data;
    }
    uint64 data() const { return _data; }

public:
    static Move intToMove(uint64 index){
        if (index < 6){
            return Move(index);
        } else if (index == 10){
            return Move::Move_NB;
        } else {
            return Move(index * 3 - 10);
        }
    }

    static uint64 moveToInt(Move move){
        if (int(move) < 6){
            return static_cast<uint64>(move);
        } if (move == Move::Move_NB){
            return 10;
        } else {
            return (static_cast<uint64>(move) + 10) / 3;
        }
    }
protected:
    uint64 _data = AllNullMove;
};


struct MutableShortenMoveList : public ShortenMoveList {
    uint32 size() const {
        for (uint32 i = 0; i < MaxLengthSupported; i++){
            if (moveAt(i) == Move::Move_NB){
                return i;
            }
        }
        return MaxLengthSupported;
    }
    void prepend(Move move){
        uint32 s = size();
        if (s < MaxLengthSupported){
            for (int i = MaxLengthSupported - 1; i > 0; i--){
                setMoveAt(i, moveAt(i - 1));
            }
            setMoveAt(0, move);
        }
    }
    void add(Move move){
        uint32 s = size();
        if (s < MaxLengthSupported){
            setMoveAt(s, move);
        }
    }
    std::vector<Move> toVector() const {
        std::vector<Move> out;
        for (int i = 0; i < size(); i++){
            out.push_back(moveAt(i));
        }
        return out;
    }

    bool operator==(MutableShortenMoveList m) const { return m._data != _data; }
};

}
