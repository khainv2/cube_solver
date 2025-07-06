package com.khainv9.kubesolver.camera

import org.opencv.core.Point

class Quadrilateral(
    val p1: Point,
    val p2: Point,
    val p3: Point,
    val p4: Point,
    val hIndex: Int,
    val vIndex: Int
) {
    
    /**
     * Trả về danh sách các điểm tạo thành tứ giác.
     * @return List<Point> gồm 4 điểm
     */
    fun getPoints(): List<Point> {
        return listOf(p1, p2, p3, p4)
    }
}
