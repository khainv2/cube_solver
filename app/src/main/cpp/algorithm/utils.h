#pragma once
#include <bitset>
#include "defines.h"
#include <vector>
#include <chrono>

namespace kube {
using namespace std::chrono;
using time_point = std::chrono::steady_clock::time_point;
using ms = std::chrono::milliseconds;
using us = std::chrono::microseconds;
using ns = std::chrono::nanoseconds;

template <typename timeunit>
constexpr int64 duration_of(const time_point &from, const time_point &to){
    return std::chrono::duration_cast<timeunit>(to - from).count();
}

template<typename T> constexpr void swap_ring(T &a1, T &a2, T &a3, T &a4){
    T t = a1;
    a1 = a2;
    a2 = a3;
    a3 = a4;
    a4 = t;
}

template <typename T> constexpr int find_index(const T *t_array, int size,
                                               T match_value){
    for (int i = 0; i < size; i++){
        if (t_array[i] == match_value){
            return i;
        }
    }
    return -1;
}
std::vector<std::string> split(const std::string& str, const std::string& delim);
bool replace(std::string& str, const std::string& from, const std::string& to);
template <typename T>
bool vt_contains(const std::vector<T> &vector, T value){
    return std::find(vector.begin(), vector.end(), value) != vector.end();
}
template <typename T>
int vt_find_index(const std::vector<T> &vector, T value){
    return std::find(vector.begin(), vector.end(), value) - vector.begin();
}

// Tính toán giá trị giai thừa ngay từ thời điểm compile
template<int N> class Factorial
{ public: static const int Result = Factorial<N-1>::Result * N; };
template<> class Factorial<0>
{ public: static const int Result = 1; };

// Lấy giá trị 2^x ngay thời điểm compile code
template<int N> class PowerOfTwo
{ public: static const int Result = PowerOfTwo<N-1>::Result * 2; };
template<> class PowerOfTwo<0>
{ public: static const int Result = 1; };

template<int N> class PowerOfThree
{ public: static const int Result = PowerOfThree<N-1>::Result * 3; };
template<> class PowerOfThree<0>
{ public: static const int Result = 1; };

/// Bảng tra giá trị 3 ^ x
constexpr uint32 P3[] = {
    PowerOfThree<0>::Result,
    PowerOfThree<1>::Result,
    PowerOfThree<2>::Result,
    PowerOfThree<3>::Result,
    PowerOfThree<4>::Result,
    PowerOfThree<5>::Result,
    PowerOfThree<6>::Result,
};

template<uint64 N> struct PowerOfEleven
{ static const uint64 Result = PowerOfEleven<N-1>::Result * 11; };
template<> struct PowerOfEleven<0>
{ static const uint64 Result = 1 ; };

/// Bảng tra giá trị 11 ^ x
constexpr uint64 P11[] = {
    PowerOfEleven<0>::Result,
    PowerOfEleven<1>::Result,
    PowerOfEleven<2>::Result,
    PowerOfEleven<3>::Result,
    PowerOfEleven<4>::Result,
    PowerOfEleven<5>::Result,
    PowerOfEleven<6>::Result,
    PowerOfEleven<7>::Result,
    PowerOfEleven<8>::Result,
    PowerOfEleven<9>::Result,
    PowerOfEleven<10>::Result,
    PowerOfEleven<11>::Result,
    PowerOfEleven<12>::Result,
    PowerOfEleven<13>::Result,
    PowerOfEleven<14>::Result,
    PowerOfEleven<15>::Result,
    PowerOfEleven<16>::Result,
    PowerOfEleven<17>::Result,
};

constexpr uint32 PowerOf11(uint32 n){
    if (n == 0) return 1;
    else return 11 * PowerOf11(n - 1);
}

template<int N> class PowerOfEighteen
{ public: static const int Result = PowerOfEighteen<N-1>::Result * 18; };

template<> class PowerOfEighteen<0>
{ public: static const int Result = 1; };
/// Bảng tra giá trị 18 ^ x
constexpr uint32 P18[] = {
    PowerOfEighteen<0>::Result,
    PowerOfEighteen<1>::Result,
    PowerOfEighteen<2>::Result,
    PowerOfEighteen<3>::Result,
    PowerOfEighteen<4>::Result,
    PowerOfEighteen<5>::Result,
    PowerOfEighteen<6>::Result,
    PowerOfEighteen<7>::Result,
};
template<int N> class PowerOfNineteen
{ public: static const int Result = PowerOfNineteen<N-1>::Result * 19; };

template<> class PowerOfNineteen<0>
{ public: static const int Result = 1; };
/// Bảng tra giá trị 18 ^ x
constexpr uint32 P19[] = {
    PowerOfNineteen<0>::Result,
    PowerOfNineteen<1>::Result,
    PowerOfNineteen<2>::Result,
    PowerOfNineteen<3>::Result,
    PowerOfNineteen<4>::Result,
    PowerOfNineteen<5>::Result,
    PowerOfNineteen<6>::Result,
    PowerOfNineteen<7>::Result,
};


namespace Bitwise {
template<typename T> constexpr bool isBitOn(T value, int bitIndex){
    return (value & (1 << bitIndex)) > 0;
}
template<typename T> constexpr char bitOn(T value, int bitIndex){
    return (value >> bitIndex) & 1;
}
template<typename T> constexpr void setBit(T &value, int bitIndex){
    value |= (1 << bitIndex);
}
template<typename T> constexpr void clearBit(T &value, int bitIndex){
    value &= ~(1 << bitIndex);
}
template<typename T> constexpr int countSetBit(T n){
    int count = 0;
    for (; n; count++)
        n = n & (n - 1); // clear the least significant bit set
    return count;
}
}

/**
 * @brief nchoosek thuật toán tính tổ hợp, trong @param n chọn @param k
 * @return số lượng trường hợp có thể chọn được
 */
constexpr int nchoosek(int n, int k){
    if (n < k) return 0;
    if (k == 0) return 1;
    return (n * nchoosek(n - 1, k - 1)) / k;
}

/**
 * @brief positionToCombination tính tổ hợp từ giá trị index
 * https://math.stackexchange.com/questions/1363239/fast-way-to-get-a-position-of-combination-without-repetitions/1364041?
 */
constexpr uint16 combination_to_position(uint16 combination, int N){
    int r = 0; // Tổng số bit on tại vị trí n
    uint16 p = 0; // Giá trị đầu ra
    for (int n = 0; n < N; n++){
        char bitOn = Bitwise::bitOn(combination, n);
        r += bitOn;
        p += bitOn * nchoosek(n, r);
    }
    return p;
}

constexpr uint16 position_to_combination(int n, int r, int m){
    uint16 out = 0;
    while (n > 0){
        int y = 0;
        if (n > r && r >= 0)
            y = nchoosek(n - 1,r);

        if (m >= y){
            m = m - y;
            Bitwise::setBit(out, n);
            r = r - 1;
        } else {
            Bitwise::clearBit(out, n);
        }
        n = n - 1;
    }
    return out >> 1;
}

constexpr int factorial(int n){
    if (n == 0){
        return 1;
    } else {
        return n * factorial(n - 1);
    }
}

template<int Count, typename StoreType>
struct PermutationTranslation {
private:
    static constexpr int FactorialCount = factorial(Count);

public:
    /**
     * @brief permutation8ToPosition trả về index của một hoán vị
     */
    static StoreType toPosition(std::vector<int> permutation){
        StoreType output = 0;
        // Đảm bảo các giá trị đầu vào chỉ nằm trong khoảng từ 0->7
        // ví dụ 0 6 2 3 5 4 1 7
        for (int value = 0; value < Count; value++){
            // Tìm vị trí của từng value (vd số 2 có index = 2)
            auto indexOfValue = vt_find_index(permutation, value);
            // Tìm số lượng các số lớn hơn ở phía TRÁI (vd số 2 có 1 số lớn hơn nó ở bên trái)
            int countLarger = 0;
            for (int index = 0; index < indexOfValue; index++){
                if (permutation[index] > value){
                    countLarger++;
                }
            }

            const StoreType positionOfValue = static_cast<StoreType>(countLarger);
            const StoreType factor = FactorialCount
                    / factorial(static_cast<StoreType>(Count - value));
            output += (positionOfValue * factor);
        }
        return output;
    }

    static std::vector<int> fromPosition(StoreType position){
        // Khởi tạo giá trị mặc định
        std::vector<int> output(Count);
        for (int i = 0; i < Count; i++){
            output[i] = -1;
        }

        for (int value = 0; value < Count; value++){
            const StoreType factor = FactorialCount
                    / factorial(static_cast<StoreType>(Count - value));
            const int positionOfValue = (position / factor) % (Count - value);

            int countNullidx = 0;
            for (int i = 0; i < Count; i++){
                if (output[i] == -1){
                    if (positionOfValue == countNullidx){
                        output[i] = value;
                        break;
                    }
                    countNullidx++;
                }
            }
        }
        return output;
    }
};

}
















