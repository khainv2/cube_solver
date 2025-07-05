#ifndef CUBEDISPLAYER_H
#define CUBEDISPLAYER_H

#include <QGLWidget>
#include "openglutils.h"
#include "mousestate.h"
#include "algorithm/defines.h"

class CubeDisplayer : public QGLWidget {
    Q_OBJECT

    enum {
        ViewportSize = 1000,
        CubeDefaultSize = 120,

        AngleX_Min = -60,
        AngleX_Max = +80,
    };
    /// Các trục của cục rubik
    enum class Axis { X, Y, Z };

public:
    CubeDisplayer(QWidget *parent = nullptr);

    kube::ColorfulCube cube() const;
    void setCube(const kube::ColorfulCube &cube);

    RotationState rotationState() const;
    void setRotationState(const RotationState &rotationState);

protected:
    void initializeGL() override;
    void resizeGL(int width, int height) override;
    void paintGL() override;

protected:
    void mousePressEvent(QMouseEvent *event) override;
    void mouseMoveEvent(QMouseEvent *event) override;

protected:
    int _cubeBlockSize = CubeDefaultSize;
    kube::ColorfulCube _cube;

    RotationState _rotationState;

    QPoint _lastPos;
    RotationState _lastRotationState;

};
#endif // CUBEDISPLAYER_H
