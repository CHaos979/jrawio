package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 椭圆/圆形图形实现类
 */
public class OvalShape extends BlockShape {
    
    public OvalShape(double width, double height) {
        super(width, height);
    }
    
    @Override
    public void drawShape(GraphicsContext gc, double x, double y, double width, double height) {
        // 设置填充颜色为白色
        gc.setFill(Color.WHITE);
        
        // 设置边框样式
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        
        // 先填充椭圆/圆形
        gc.fillOval(x, y, width, height);
        
        // 再绘制边框
        gc.strokeOval(x, y, width, height);
    }
}
