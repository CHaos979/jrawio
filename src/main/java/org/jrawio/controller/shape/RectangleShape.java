package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 矩形图形实现类
 */
public class RectangleShape extends Shape {
    
    public RectangleShape(double width, double height) {
        super(width, height);
    }
    
    @Override
    protected void drawShape(GraphicsContext gc, double x, double y, double width, double height) {
        // 设置绘制样式
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        
        // 绘制矩形
        gc.strokeRect(x, y, width, height);
    }
}
