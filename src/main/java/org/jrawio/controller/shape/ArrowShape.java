package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

/**
 * 箭头形状类
 * 包含起始点和结束点属性，绘制从起始点指向结束点的箭头
 */
public class ArrowShape extends Shape {
    
    /** 起始点坐标（相对于Shape的坐标系） */
    private Point2D startPoint;
    
    /** 结束点坐标（相对于Shape的坐标系） */
    private Point2D endPoint;
    
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
        // 默认箭头从左上角指向右下角
        initializePoints(width, height);
        draw(); // 重新绘制以应用初始点
    }
    
    /**
     * 构造函数 - 指定起始点和结束点
     * 
     * @param width      图形宽度
     * @param height     图形高度
     * @param startPoint 起始点
     * @param endPoint   结束点
     */
    public ArrowShape(double width, double height, Point2D startPoint, Point2D endPoint) {
        super(width, height);
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        draw(); // 重新绘制以应用指定的点
    }
    
    /**
     * 初始化默认的起始点和结束点
     * 
     * @param width  图形宽度
     * @param height 图形高度
     */
    private void initializePoints(double width, double height) {
        // 让箭头从左侧中心指向右侧中心，这样看起来更美观
        this.startPoint = new Point2D(width * 0.15, height * 0.5);
        this.endPoint = new Point2D(width * 0.85, height * 0.5);
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
    
    /**
     * 获取起始点
     * 
     * @return 起始点坐标
     */
    public Point2D getStartPoint() {
        return startPoint;
    }
    
    /**
     * 设置起始点
     * 
     * @param startPoint 起始点坐标
     */
    public void setStartPoint(Point2D startPoint) {
        this.startPoint = startPoint;
        draw();
    }
    
    /**
     * 获取结束点
     * 
     * @return 结束点坐标
     */
    public Point2D getEndPoint() {
        return endPoint;
    }
    
    /**
     * 设置结束点
     * 
     * @param endPoint 结束点坐标
     */
    public void setEndPoint(Point2D endPoint) {
        this.endPoint = endPoint;
        draw();
    }
    
    /**
     * 设置箭头的起始点和结束点
     * 
     * @param startPoint 起始点坐标
     * @param endPoint   结束点坐标
     */
    public void setArrowPoints(Point2D startPoint, Point2D endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        draw();
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
