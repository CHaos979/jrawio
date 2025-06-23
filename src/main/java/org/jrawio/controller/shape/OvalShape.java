package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

/**
 * 椭圆/圆形图形实现类
 */
public class OvalShape extends BlockShape {
    
    public OvalShape(double width, double height) {
        super(width, height);
    }
    
    /**
     * 拷贝构造方法
     * 创建一个与源OvalShape具有相同属性的新OvalShape实例
     * 
     * @param source 源OvalShape对象
     */
    public OvalShape(OvalShape source) {
        super(source);
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

    @Override
    protected Point2D calculateArrowConnectionPoint(ArrowHandleManager.ArrowHandle arrowHandle) {
        // 获取图形的布局坐标和尺寸
        double shapeLayoutX = getLayoutX();
        double shapeLayoutY = getLayoutY();
        double shapeWidth = getWidth();
        double shapeHeight = getHeight();
        
        // 计算额外空间（用于箭头控制点）
        double arrowHandleOffset = ArrowHandleManager.getArrowHandleOffset();
        double arrowHandleSize = ArrowHandleManager.getArrowHandleSize();
        double extraSpace = arrowHandleOffset + arrowHandleSize;
        double padding = 4 + extraSpace;
        
        // 计算实际椭圆区域（不包括padding）
        double actualShapeX = shapeLayoutX + padding;
        double actualShapeY = shapeLayoutY + padding;
        double actualShapeWidth = shapeWidth - 2 * padding;
        double actualShapeHeight = shapeHeight - 2 * padding;
        
        // 椭圆中心点
        double centerX = actualShapeX + actualShapeWidth / 2;
        double centerY = actualShapeY + actualShapeHeight / 2;
          // 椭圆的半长轴和半短轴
        double radiusX = actualShapeWidth / 2;
        double radiusY = actualShapeHeight / 2;
        
        // 向内偏移的比例，使箭头起点稍微偏离边界
        double insetRatio = 0.95; // 5% 向内偏移
        
        // 根据箭头控制点类型计算椭圆边界上的精确连接点（带向内偏移）
        switch (arrowHandle) {
            case ARROW_TOP:
                // 椭圆顶部点，向内偏移
                return new javafx.geometry.Point2D(centerX, centerY - radiusY * insetRatio);
                
            case ARROW_BOTTOM:
                // 椭圆底部点，向内偏移
                return new javafx.geometry.Point2D(centerX, centerY + radiusY * insetRatio);
                
            case ARROW_LEFT:
                // 椭圆左侧点，向内偏移
                return new javafx.geometry.Point2D(centerX - radiusX * insetRatio, centerY);
                
            case ARROW_RIGHT:
                // 椭圆右侧点，向内偏移
                return new javafx.geometry.Point2D(centerX + radiusX * insetRatio, centerY);
                
            default:
                // 默认返回椭圆中心点
                return new javafx.geometry.Point2D(centerX, centerY);
        }
    }

    @Override
    protected Point2D findNearestSnapPoint(Point2D mousePoint, double snapRadius) {
        // 获取图形的布局坐标和尺寸
        double shapeWidth = getWidth();
        double shapeHeight = getHeight();
        
        // 计算额外空间（用于箭头控制点）
        double arrowHandleOffset = ArrowHandleManager.getArrowHandleOffset();
        double arrowHandleSize = ArrowHandleManager.getArrowHandleSize();
        double extraSpace = arrowHandleOffset + arrowHandleSize;
        double padding = 4 + extraSpace;
        
        // 计算实际椭圆区域（相对于shape本地坐标）
        double actualShapeX = padding;
        double actualShapeY = padding;
        double actualShapeWidth = shapeWidth - 2 * padding;
        double actualShapeHeight = shapeHeight - 2 * padding;
        
        // 椭圆中心点
        double centerX = actualShapeX + actualShapeWidth / 2;
        double centerY = actualShapeY + actualShapeHeight / 2;
        
        // 椭圆的半长轴和半短轴
        double radiusX = actualShapeWidth / 2;
        double radiusY = actualShapeHeight / 2;
        
        // 定义椭圆的四个关键吸附点
        Point2D topPoint = new Point2D(centerX, centerY - radiusY);      // 上点
        Point2D bottomPoint = new Point2D(centerX, centerY + radiusY);   // 下点
        Point2D leftPoint = new Point2D(centerX - radiusX, centerY);     // 左点
        Point2D rightPoint = new Point2D(centerX + radiusX, centerY);    // 右点
        
        Point2D[] snapPoints = {topPoint, bottomPoint, leftPoint, rightPoint};
        
        // 查找距离鼠标最近且在吸附半径内的点
        Point2D nearestPoint = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Point2D snapPoint : snapPoints) {
            double distance = mousePoint.distance(snapPoint);
            if (distance <= snapRadius && distance < minDistance) {
                minDistance = distance;
                nearestPoint = snapPoint;
            }
        }
        
        return nearestPoint;
    }
}
