package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 椭圆/圆形图形实现类
 */
public class OvalShape extends Shape {
    
    public OvalShape(double width, double height) {
        super(width, height);
    }
    
    @Override
    public void drawShape(GraphicsContext gc, double x, double y, double width, double height) {
        // 设置绘制样式
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        
        // 绘制椭圆/圆形
        gc.strokeOval(x, y, width, height);
    }
}
