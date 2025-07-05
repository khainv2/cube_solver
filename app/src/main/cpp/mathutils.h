#ifndef MATHUTILS_H
#define MATHUTILS_H
#include <QSize>
#include <QPoint>
#include <QRect>

struct RotationState {
    int x = 20;
    int y = 0;
    int z = 0;

    RotationState(){}
    RotationState(int x, int y, int z):
        x(x), y(y), z(z){
        normalize();
    }

    void normalize(){
        while (x < -180)
            x += 360;
        while (x > +180)
            x -= 360;
        while (y < -180)
            y += 360;
        while (y > +180)
            y -= 360;
        while (z < -180)
            z += 360;
        while (z > +180)
            z -= 360;
    }
};

class Size3D : public QSize {

public:

    Q_DECL_CONSTEXPR inline Size3D() Q_DECL_NOTHROW : QSize(), dp(-1) {}

    Q_DECL_CONSTEXPR inline Size3D(int w, int h, int d) Q_DECL_NOTHROW : QSize(w, h), dp(d) {}

    Q_DECL_CONSTEXPR inline int depth() const Q_DECL_NOTHROW
    { return dp; }

private:
    int dp;
};



class Point3D : public QPoint {

public:
    Point3D(): QPoint(){
        zp = 0;
    }

    Point3D(int x, int y, int z): QPoint(x, y){
        zp = z;
    }

    inline int z() const { return zp; }
    void setZ(int z){ zp = z;}

//    Q_DECL_CONSTEXPR inline bool operator==(const Point3D &p1, const Point3D &p2)
//    { return p1.xp == p2.xp && p1.yp == p2.yp && p1.zp == p2.zp; }

private:
    int zp;
};


class Rectangular : public QRect {

public:
    Rectangular(): QRect(){
        z1 = 0;
        z2 = -1;
    }

    Q_DECL_CONSTEXPR inline Rectangular(int aleft, int atop, int afront, int awidth, int aheight, int adepth) Q_DECL_NOTHROW
        : QRect(aleft, atop, awidth, aheight),
          z1(afront),
          z2(afront + adepth -1) {}


    Q_DECL_CONSTEXPR inline int z() const Q_DECL_NOTHROW
    { return z1; }


    Q_DECL_CONSTEXPR inline int depth() const Q_DECL_NOTHROW
    { return  z2 - z1 + 1; }


    Q_DECL_CONSTEXPR inline int nnear() const Q_DECL_NOTHROW
    { return z1; }

    Q_DECL_CONSTEXPR inline int ffar() const Q_DECL_NOTHROW
    { return z2; }

    Point3D topLeftNear() const Q_DECL_NOTHROW {
        return Point3D(topLeft().x(), topLeft().y(), z1);
    }

    Point3D bottomRightFar() const Q_DECL_NOTHROW {
        return Point3D(bottomRight().x(), bottomRight().y(), z2);
    }

private:
    int z1;
    int z2;
};
#endif // MATHUTILS_H
