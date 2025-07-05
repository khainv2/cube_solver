#include "cubedisplayer.h"

#include <QDebug>
#include <QElapsedTimer>
#include <QTimer>

using namespace kube;
CubeDisplayer::CubeDisplayer(QWidget *parent): QGLWidget(parent){
    _rotationState = RotationState(20, 30, 0);
}

void CubeDisplayer::initializeGL() {
    OpenGLUtils::init();
}

void CubeDisplayer::resizeGL(int width, int height) {
    Rectangular scope(-ViewportSize / 2, -ViewportSize / 2, -ViewportSize / 2,
                      ViewportSize, ViewportSize, ViewportSize);
    OpenGLUtils::setViewport(width, height, scope);
}

void CubeDisplayer::mousePressEvent(QMouseEvent *event) {
    _lastPos = event->pos();
    _lastRotationState = _rotationState;

    QGLWidget::mousePressEvent(event);
}

void CubeDisplayer::mouseMoveEvent(QMouseEvent *event) {
    auto dx = event->x() - _lastPos.x();
    auto dy = event->y() - _lastPos.y();

    if (event->buttons() & Qt::LeftButton) {
        RotationState newRot(_lastRotationState.x + dy / 5,
                             _lastRotationState.y + dx / 5,
                             _lastRotationState.z);
        setRotationState(newRot);
        update();
    }

    QGLWidget::mouseMoveEvent(event);
}

RotationState CubeDisplayer::rotationState() const
{
    return _rotationState;
}

void CubeDisplayer::setRotationState(const RotationState &rotationState)
{
    _rotationState = rotationState;
    if (_rotationState.x < AngleX_Min)
        _rotationState.x = AngleX_Min;
    if (_rotationState.x > AngleX_Max)
        _rotationState.x = AngleX_Max;
}

void CubeDisplayer::paintGL() {
    OpenGLUtils::setViewportState(_rotationState);

    int cubeSize = _cubeBlockSize * 3;
    for (int face = 0; face < ColorfulCube::Color::COLOR_NB; face++){
        Point3D faceTL;
        Point3D faceBR;
        Axis axis;
        switch (ColorfulCube::Color(face)){
        case ColorfulCube::Color::RED:
            faceTL = Point3D(-cubeSize / 2, +cubeSize / 2, -cubeSize / 2);
            faceBR = Point3D(+cubeSize / 2, +cubeSize / 2, +cubeSize / 2);
            axis = Axis::Y;
            break;
        case ColorfulCube::Color::BLUE:
            faceTL = Point3D(-cubeSize / 2, +cubeSize / 2, -cubeSize / 2);
            faceBR = Point3D(-cubeSize / 2, -cubeSize / 2, +cubeSize / 2);
            axis = Axis::X;
            break;
        case ColorfulCube::Color::WHITE:
            faceTL = Point3D(-cubeSize / 2, +cubeSize / 2, +cubeSize / 2);
            faceBR = Point3D(+cubeSize / 2, -cubeSize / 2, +cubeSize / 2);
            axis = Axis::Z;
            break;
        case ColorfulCube::Color::GREEN:
            faceTL = Point3D(+cubeSize / 2, +cubeSize / 2, +cubeSize / 2);
            faceBR = Point3D(+cubeSize / 2, -cubeSize / 2, -cubeSize / 2);
            axis = Axis::X;
            break;
        case ColorfulCube::Color::YELLOW:
            faceTL = Point3D(+cubeSize / 2, +cubeSize / 2, -cubeSize / 2);
            faceBR = Point3D(-cubeSize / 2, -cubeSize / 2, -cubeSize / 2);
            axis = Axis::Z;
            break;
        case ColorfulCube::Color::ORANGE:
            faceTL = Point3D(-cubeSize / 2, -cubeSize / 2, +cubeSize / 2);
            faceBR = Point3D(+cubeSize / 2, -cubeSize / 2, -cubeSize / 2);
            axis = Axis::Y;
            break;
        default: break;
        }

        for (int piece = 0; piece < ColorfulCube::TilePos_NB; piece++){
            // Giá trị màu từng ô
            ColorfulCube::Color color = _cube.colors[face][piece];
            QColor uiColor = OpenGLUtils::colorToUiColor(color);

            Point3D pTL;
            Point3D pTR;
            Point3D pBR;
            Point3D pBL;
            int row = piece / 3;
            int column = piece % 3;
            switch (axis){
            case Axis::X:
                pTL = Point3D(faceTL.x(),
                              faceTL.y() + row * ((faceBR.y() - faceTL.y()) / 3),
                              faceTL.z() + column * ((faceBR.z() - faceTL.z()) / 3));
                pTR = Point3D(faceTL.x(),
                              faceTL.y() + row * ((faceBR.y() - faceTL.y()) / 3),
                              faceTL.z() + (column + 1) * ((faceBR.z() - faceTL.z()) / 3));
                pBR = Point3D(faceTL.x(),
                              faceTL.y() + (row + 1) * ((faceBR.y() - faceTL.y()) / 3),
                              faceTL.z() + (column + 1) * ((faceBR.z() - faceTL.z()) / 3));
                pBL = Point3D(faceTL.x(),
                              faceTL.y() + (row + 1) * ((faceBR.y() - faceTL.y()) / 3),
                              faceTL.z() + column * ((faceBR.z() - faceTL.z()) / 3));
            break;
            case Axis::Y:
                pTL = Point3D(faceTL.x() + column * ((faceBR.x() - faceTL.x()) / 3),
                              faceTL.y(),
                              faceTL.z() + row * ((faceBR.z() - faceTL.z()) / 3));
                pTR = Point3D(faceTL.x() + (column + 1) * ((faceBR.x() - faceTL.x()) / 3),
                              faceTL.y(),
                              faceTL.z() + row * ((faceBR.z() - faceTL.z()) / 3));
                pBR = Point3D(faceTL.x() + (column + 1) * ((faceBR.x() - faceTL.x()) / 3),
                              faceTL.y(),
                              faceTL.z() + (row + 1) * ((faceBR.z() - faceTL.z()) / 3));
                pBL = Point3D(faceTL.x() + column * ((faceBR.x() - faceTL.x()) / 3),
                              faceTL.y(),
                              faceTL.z() + (row + 1) * ((faceBR.z() - faceTL.z()) / 3));
                break;
            case Axis::Z:
                pTL = Point3D(faceTL.x() + column * ((faceBR.x() - faceTL.x()) / 3),
                              faceTL.y() + row * ((faceBR.y() - faceTL.y()) / 3),
                              faceTL.z());
                pTR = Point3D(faceTL.x() + (column + 1) * ((faceBR.x() - faceTL.x()) / 3),
                              faceTL.y() + row * ((faceBR.y() - faceTL.y()) / 3),
                              faceTL.z());
                pBR = Point3D(faceTL.x() + (column + 1) * ((faceBR.x() - faceTL.x()) / 3),
                              faceTL.y() + (row + 1) * ((faceBR.y() - faceTL.y()) / 3),
                              faceTL.z());
                pBL = Point3D(faceTL.x() + column * ((faceBR.x() - faceTL.x()) / 3),
                              faceTL.y() + (row + 1) * ((faceBR.y() - faceTL.y()) / 3),
                              faceTL.z());
                break;
            }

            OpenGLUtils::drawFace(uiColor, {pTL, pTR, pBR, pBL});

            glLineWidth(2);
            glColor3i(0, 0, 0);
            glBegin(GL_LINE_LOOP);
            glVertex3i(pTL.x(), pTL.y(), pTL.z());
            glVertex3i(pTR.x(), pTR.y(), pTR.z());
            glVertex3i(pBR.x(), pBR.y(), pBR.z());
            glVertex3i(pBL.x(), pBL.y(), pBL.z());
            glEnd();
        }
    }
}
ColorfulCube CubeDisplayer::cube() const
{
    return _cube;
}

void CubeDisplayer::setCube(const ColorfulCube &cube)
{
    _cube = cube;
}
