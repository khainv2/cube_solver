QT       += core gui opengl

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

CONFIG += c++14 console

# The following define makes your compiler emit warnings if you use
# any Qt feature that has been marked deprecated (the exact warnings
# depend on your compiler). Please consult the documentation of the
# deprecated API in order to know how to port your code away from it.
DEFINES += QT_DEPRECATED_WARNINGS

# You can also make your code fail to compile if it uses deprecated APIs.
# In order to do so, uncomment the following line.
# You can also select to disable deprecated APIs only up to a certain version of Qt.
#DEFINES += QT_DISABLE_DEPRECATED_BEFORE=0x060000    # disables all the APIs deprecated before Qt 6.0.0

LIBS += gdi32.lib dwmapi.lib
LIBS += -luser32
LIBS += -lOpengl32

#include(sparsehash/sparsehash.pri)

SOURCES += \
    algorithm/blockcube.cpp \
    algorithm/colorfulcube.cpp \
    algorithm/defines.cpp \
    algorithm/move.cpp \
    algorithm/permutationphase.cpp \
    algorithm/rotationphase.cpp \
    algorithm/solver.cpp \
    algorithm/utils.cpp \
    cubedisplayer.cpp \
    main.cpp \
    mainwindow.cpp \
    mathutils.cpp \
    mousestate.cpp \
    openglutils.cpp

HEADERS += \
    algorithm/blockcube.h \
    algorithm/colorfulcube.h \
    algorithm/defines.h \
    algorithm/move.h \
    algorithm/permutationphase.h \
    algorithm/rotationphase.h \
    algorithm/solver.h \
    algorithm/utils.h \
    cubedisplayer.h \
    mainwindow.h \
    mathutils.h \
    mousestate.h \
    openglutils.h

FORMS += \
    mainwindow.ui

# Default rules for deployment.
qnx: target.path = /tmp/$${TARGET}/bin
else: unix:!android: target.path = /opt/$${TARGET}/bin
!isEmpty(target.path): INSTALLS += target
