package com.khainv9.kubesolver.camera;

import org.opencv.core.Point;
import java.util.ArrayList;
import java.util.List;

public class Quadrilateral {
    private Point p1;
    private Point p2;
    private Point p3;
    private Point p4;
    public int hIndex;
    public int vIndex;

    public Quadrilateral(Point p1, Point p2, Point p3, Point p4, int hIndex, int vIndex) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
        this.hIndex = hIndex;
        this.vIndex = vIndex;
    }

    /**
     * Trả về danh sách các điểm tạo thành tứ giác.
     * @return List<Point> gồm 4 điểm
     */
    public List<Point> getPoints() {
        List<Point> points = new ArrayList<>();
        points.add(p1);
        points.add(p2);
        points.add(p3);
        points.add(p4);
        return points;
    }

    // Các getter riêng nếu cần truy xuất từng điểm
    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

    public Point getP3() {
        return p3;
    }

    public Point getP4() {
        return p4;
    }
}
