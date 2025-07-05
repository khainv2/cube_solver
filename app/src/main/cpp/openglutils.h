#ifndef OPENGLUTILS_H
#define OPENGLUTILS_H

#include "mathutils.h"
#include "algorithm/defines.h"
#include "algorithm/colorfulcube.h"

#include <QColor>

class OpenGLUtils
{
public:
    static void init();
    static void setViewport(int width, int height, const Rectangular &r);
    static void setViewportState(const RotationState &state);
    static void drawFace(const QColor &color,
                          QList<Point3D> points);


    static void drawRectangular(const QColor &color, const Rectangular &rect);

//    static void limitAngle(int &angle, int )

    static QColor colorToUiColor(const kube::ColorfulCube::Color &color);
};

#endif // OPENGLUTILS_H
