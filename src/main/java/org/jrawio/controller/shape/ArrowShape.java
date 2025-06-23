package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

/**
 * 箭头形状类
 * 继承LineShape，在基础线形上添加箭头头部
 */
public class ArrowShape extends LineShape {
    
    /** 箭头头部的长度 */
    private static final double ARROW_HEAD_LENGTH = 5.0;
    
    /** 箭头头部的角度（弧度） */
    private static final double ARROW_HEAD_ANGLE = Math.PI / 6; // 30度
    
    /**
     * 构造函数
     * 
     * @param width  图形宽度
     * @param height 图形高度
     */
    public ArrowShape(double width, double height) {
        super(width, height);
    }
    
    /**
     * 拷贝构造方法
     * 创建一个与源ArrowShape具有相同属性的新ArrowShape实例
     * 
     * @param source 源ArrowShape对象
     */
    public ArrowShape(ArrowShape source) {
        super(source);
    }
    
    /**
     * 根据两个点创建箭头的构造函数
     * 会自动计算合适的canvas大小
     * 
     * @param startPoint 起始点（绝对坐标）
     * @param endPoint   结束点（绝对坐标）
     */
    public ArrowShape(Point2D startPoint, Point2D endPoint) {
        super(startPoint, endPoint);
    }
    
    /**
     * 绘制箭头
     * 
     * @param gc     图形上下文
     * @param x      绘制起始x坐标
     * @param y      绘制起始y坐标
     * @param width  绘制宽度
     * @param height 绘制高度
     */
    @Override
    public void drawShape(GraphicsContext gc, double x, double y, double width, double height) {
        // 如果起始点或结束点为空，初始化默认值
        if (startPoint == null || endPoint == null) {
            initializePoints(getWidth(), getHeight());
        }
        
        // 设置绘制属性
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.setFill(Color.BLACK);
        
        // 计算实际的起始点和结束点坐标
        double actualStartX = x + (startPoint.getX() / getWidth()) * width;
        double actualStartY = y + (startPoint.getY() / getHeight()) * height;
        double actualEndX = x + (endPoint.getX() / getWidth()) * width;
        double actualEndY = y + (endPoint.getY() / getHeight()) * height;
        
        // 绘制箭头主线
        gc.strokeLine(actualStartX, actualStartY, actualEndX, actualEndY);
        
        // 计算箭头头部
        drawArrowHead(gc, actualStartX, actualStartY, actualEndX, actualEndY);
    }
    
    /**
     * 绘制箭头头部
     * 
     * @param gc      图形上下文
     * @param startX  起始点X坐标
     * @param startY  起始点Y坐标
     * @param endX    结束点X坐标
     * @param endY    结束点Y坐标
     */
    private void drawArrowHead(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        // 计算箭头方向角度
        double angle = Math.atan2(endY - startY, endX - startX);
        
        // 计算箭头头部的两个端点
        double arrowX1 = endX - ARROW_HEAD_LENGTH * Math.cos(angle - ARROW_HEAD_ANGLE);
        double arrowY1 = endY - ARROW_HEAD_LENGTH * Math.sin(angle - ARROW_HEAD_ANGLE);
        
        double arrowX2 = endX - ARROW_HEAD_LENGTH * Math.cos(angle + ARROW_HEAD_ANGLE);
        double arrowY2 = endY - ARROW_HEAD_LENGTH * Math.sin(angle + ARROW_HEAD_ANGLE);
        
        // 绘制箭头头部（三角形）
        gc.strokeLine(endX, endY, arrowX1, arrowY1);
        gc.strokeLine(endX, endY, arrowX2, arrowY2);
        
        // 可选：填充箭头头部
        double[] xPoints = {endX, arrowX1, arrowX2};
        double[] yPoints = {endY, arrowY1, arrowY2};
        gc.fillPolygon(xPoints, yPoints, 3);
    }

    @Override
    public String toString() {
        return "ArrowShape{" +
                "startPoint=" + startPoint +
                ", endPoint=" + endPoint +
                ", width=" + getWidth() +
                ", height=" + getHeight() +
                ", selected=" + selected +
                '}';
    }
}
