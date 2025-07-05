#include "openglutils.h"
#include <QGLWidget>

void OpenGLUtils::init()
{
    glEnable(GL_DEPTH_TEST);
    glShadeModel(GL_SMOOTH);
    glEnable(GL_LIGHT0);
    glEnable (GL_COLOR_MATERIAL);
    static GLfloat lightPosition[4] = { 0, 0, 10, 1.0 };
    glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
}

void OpenGLUtils::setViewport(int width, int height,
                              const Rectangular &r)
{
    int side = qMin(width, height);
    glViewport((width - side) / 2, (height - side) / 2, side, side);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();

    glOrtho(r.left(), r.right(),
            r.top (), r.bottom(),
            r.nnear(), r.ffar());
    glMatrixMode(GL_MODELVIEW);
}

void OpenGLUtils::setViewportState(const RotationState &state)
{
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glLoadIdentity();
    glTranslated(0, 0, 0);
    glRotatef(state.x, 1.0, 0.0, 0.0);
    glRotatef(state.y, 0.0, 1.0, 0.0);
    glRotatef(state.z, 0.0, 0.0, 1.0);
}

void OpenGLUtils::drawFace(const QColor &color,
                            QList<Point3D> points)
{
    glColor4f(color.redF(), color.greenF(), color.blueF(), color.alphaF());
    glBegin(GL_POLYGON);
    for (const auto &point: points){
        glVertex3i(point.x(), point.y(), point.z());
    }
    glEnd();
}

void OpenGLUtils::drawRectangular(const QColor &color, const Rectangular &rect) {
    Point3D tln = rect.topLeftNear();
    Point3D brf = rect.bottomRightFar();

    glColor4f(color.redF(), color.greenF(), color.blueF(), color.alphaF());
    glBegin(GL_POLYGON);

    { // front
        glVertex3i(tln.x(), tln.y(), tln.z());
        glVertex3i(brf.x(), tln.y(), tln.z());
        glVertex3i(brf.x(), brf.y(), tln.z());
        glVertex3i(tln.x(), brf.y(), tln.z());
    }

    { // back
        glVertex3i(tln.x(), tln.y(), brf.z());
        glVertex3i(brf.x(), tln.y(), brf.z());
        glVertex3i(brf.x(), brf.y(), brf.z());
        glVertex3i(tln.x(), brf.y(), brf.z());
    }

    { // left
        glVertex3i(tln.x(), tln.y(), tln.z());
        glVertex3i(tln.x(), tln.y(), brf.z());
        glVertex3i(tln.x(), brf.y(), brf.z());
        glVertex3i(tln.x(), brf.y(), tln.z());
    }

    { // right
        glVertex3i(brf.x(), tln.y(), tln.z());
        glVertex3i(brf.x(), tln.y(), brf.z());
        glVertex3i(brf.x(), brf.y(), brf.z());
        glVertex3i(brf.x(), brf.y(), tln.z());
    }

    { // top
        glVertex3i(tln.x(), tln.y(), tln.z());
        glVertex3i(brf.x(), tln.y(), tln.z());
        glVertex3i(tln.x(), tln.y(), brf.z());
        glVertex3i(brf.x(), tln.y(), brf.z());
    }

    { // bottom
        glVertex3i(tln.x(), brf.y(), tln.z());
        glVertex3i(brf.x(), brf.y(), tln.z());
        glVertex3i(tln.x(), brf.y(), brf.z());
        glVertex3i(brf.x(), brf.y(), brf.z());
    }

    glEnd();
}

QColor OpenGLUtils::colorToUiColor(const kube::ColorfulCube::Color &color)
{
    switch (color){
    case kube::ColorfulCube::Color::RED: return Qt::red;
    case kube::ColorfulCube::Color::BLUE: return Qt::blue;
    case kube::ColorfulCube::Color::GREEN: return Qt::green;
    case kube::ColorfulCube::Color::YELLOW: return Qt::yellow;
    case kube::ColorfulCube::Color::ORANGE: return QColor("#ff8800");
    default: return Qt::white;
    }
}

