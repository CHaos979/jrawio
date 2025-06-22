package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 矩形图形实现类
 */
public class RectangleShape extends BlockShape {
    
    public RectangleShape(double width, double height) {
        super(width, height);
    }
    
    @Override
    public void drawShape(GraphicsContext gc, double x, double y, double width, double height) {
        // 设置填充颜色为白色
        gc.setFill(Color.WHITE);
        
        // 设置边框样式
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        
        // 先填充矩形
        gc.fillRect(x, y, width, height);
        
        // 再绘制边框
        gc.strokeRect(x, y, width, height);
    }
}
