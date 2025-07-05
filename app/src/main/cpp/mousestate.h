#ifndef MOUSESTATE_H
#define MOUSESTATE_H

#include <QMouseEvent>
#include <QWidget>
#include <QDebug>

/*!
 * \brief The MouseState lớp mô tả trạng thái của chuột khi thao tác trên một QWidget bất kỳ
 */
class MouseState : public QObject {

    QPoint  mPressedPos;        /**< Vị trí click chuột xuống */
    QPoint  mPos;               /**< Vị trí hiện tại của chuột */
    bool    mIsLeftPressed = false;     /**< Chuột trái đang được nhấn hay không */
    bool    mIsRightPressed = false;    /**< Chuột phải đang được nhấn hay không */
    bool    mIsMidPressed = false;      /**< Chuột giữa đang được nhấn hay không */
    bool    mIsHover = false;
public:

    /*!
     * \brief setListenOn lắng nghe một đối tượng bất kỳ
     * \param w
     */
    void setListenOn(QWidget *w){
        w->installEventFilter(this);
    }

    QPoint pos() const {
        return mPos;
    }

    bool isLeftPressed() const {
        return mIsLeftPressed;
    }

    bool isRightPressed() const {
        return mIsRightPressed;
    }

    bool isMidPressed() const {
        return mIsMidPressed;
    }

    QPoint pressedPos() const {
        return mPressedPos;
    }

    bool isHover() const {
        return mIsHover;
    }

private:

    bool eventFilter(QObject *watched, QEvent *event) override {
        switch (event->type()){
        case QEvent::MouseButtonPress: {
            auto e = static_cast<QMouseEvent *>(event);
            mPressedPos = e->pos();
            switch (e->button()){
            case Qt::LeftButton:
                mIsLeftPressed = true;
                break;
            case Qt::RightButton:
                mIsRightPressed = true;
                break;
            case Qt::MidButton:
                mIsMidPressed = true;
                break;
            default: break;
            }
            break;
        }
        case QEvent::MouseButtonRelease: {
            mPressedPos = QPoint();
            mIsLeftPressed  = false;
            mIsRightPressed = false;
            mIsMidPressed   = false;
            break;
        }
        case QEvent::MouseMove: {
            auto e = static_cast<QMouseEvent *>(event);
            auto mouseEvent = static_cast<QMouseEvent *>(e);
            mPos = mouseEvent->pos();
            break;
        }
        case QEvent::Enter:
            mIsHover = true;
            break;
        case QEvent::Leave:
            mIsHover = false;
            break;
        default: break;
        }
        return QObject::eventFilter(watched, event);
    }
};

#endif // MOUSESTATE_H










