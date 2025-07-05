package com.khainv9.kubesolver.camera;


import com.khainv9.kubesolver.cubeview.ColorfulCube;
import com.khainv9.kubesolver.cubeview.CubeColor;
import com.khainv9.kubesolver.cubeview.Direction;
import com.khainv9.kubesolver.cubeview.Step;

enum Rotation {
    Deg_0, Deg_90, Deg_180, Deg_270
}

// Được sử dụng trong ứng dụng scan rubik, cho phép quét và đoán trạng thái hiện tại
public class PhaseControl {

    public Step step = Step.Step_Top;
    public static Direction[] GetDirectionByStep = {
            Direction.LeftToRight,
            Direction.BottomToTop,
            Direction.BottomToTop,
            Direction.BottomToTop,
            Direction.LeftToRight,
            Direction.LeftToRight,
    };
    CubeColor[][] colors;

    PhaseControl() {
        colors = new CubeColor[6][9];
        for (int i = 0; i < 6; i++){
            colors[i] = new CubeColor[9];
            for(int j = 0; j < 9; j++){
                colors[i][j] = CubeColor.UNKNOWN;
            }
        }
    }

    // Reset lại trạng thái quét
    void reset() {
        step = Step.Step_Top;
        for (int i = 0; i < 6; i++){
            for(int j = 0; j < 9; j++){
                colors[i][j] = CubeColor.UNKNOWN;
            }
        }
    }

    Direction getSuggestDirection() {
        return PhaseControl.GetDirectionByStep[step.ordinal()];
    }

    void updateColor(CubeColor[] colors, Rotation rot) {
        for(int i = 0; i < 9; i++){
            switch (rot){
                case Deg_0:
                    this.colors[step.ordinal()][i] = colors[i]; break;
                case Deg_90:
                    this.colors[step.ordinal()][i] = colors[6 - (i % 3) * 3 + (i / 3)]; break;
                case Deg_180:
                    this.colors[step.ordinal()][i] = colors[8 - i]; break;
                case Deg_270:
                    this.colors[step.ordinal()][i] = colors[2 - (i / 3) + (i % 3) * 3]; break;
            }
        }

    }

    void stepNext() {
        step = Step.values()[(step.ordinal() + 1) % Step.values().length];
    }

    ColorfulCube toColorfulCube() {
        ColorfulCube colorfulCube = new ColorfulCube();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                colorfulCube.getColors()[i][j] = colors[i][j];
            }
        }
        return colorfulCube;
    }
}
