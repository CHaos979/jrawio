package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

/**
 * 矩形图形实现类
 */
public class RectangleShape extends BlockShape {

    public RectangleShape(double width, double height) {
        super(width, height);
    }

    /**
     * 拷贝构造方法
     * 创建一个与源RectangleShape具有相同属性的新RectangleShape实例
     * 
     * @param source 源RectangleShape对象
     */
    public RectangleShape(RectangleShape source) {
        super(source);
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

        // 计算实际矩形区域（不包括padding）
        double actualShapeX = shapeLayoutX + padding;
        double actualShapeY = shapeLayoutY + padding;
        double actualShapeWidth = shapeWidth - 2 * padding;
        double actualShapeHeight = shapeHeight - 2 * padding;

        // 矩形中心点
        double centerX = actualShapeX + actualShapeWidth / 2;
        double centerY = actualShapeY + actualShapeHeight / 2;

        // 向内偏移的距离，使箭头起点稍微偏离边界
        double insetOffset = 2.0;

        // 根据箭头控制点类型计算矩形边界上的精确连接点（带向内偏移）
        switch (arrowHandle) {
            case ARROW_TOP:
                // 矩形顶边中心点，向内偏移
                return new javafx.geometry.Point2D(centerX, actualShapeY + insetOffset);

            case ARROW_BOTTOM:
                // 矩形底边中心点，向内偏移
                return new javafx.geometry.Point2D(centerX, actualShapeY + actualShapeHeight - insetOffset);

            case ARROW_LEFT:
                // 矩形左边中心点，向内偏移
                return new javafx.geometry.Point2D(actualShapeX + insetOffset, centerY);

            case ARROW_RIGHT:
                // 矩形右边中心点，向内偏移
                return new javafx.geometry.Point2D(actualShapeX + actualShapeWidth - insetOffset, centerY);

            default:
                // 默认返回矩形中心点
                return new javafx.geometry.Point2D(centerX, centerY);
        }
    }

    @Override
    protected Point2D findNearestSnapPoint(Point2D mousePoint, double snapRadius) {
        // 获取图形的尺寸
        double shapeWidth = getWidth();
        double shapeHeight = getHeight();
        
        // 计算额外空间（用于箭头控制点）
        double arrowHandleOffset = ArrowHandleManager.getArrowHandleOffset();
        double arrowHandleSize = ArrowHandleManager.getArrowHandleSize();
        double extraSpace = arrowHandleOffset + arrowHandleSize;
        double padding = 4 + extraSpace;
        
        // 计算实际矩形区域（相对于shape本地坐标）
        double actualShapeX = padding;
        double actualShapeY = padding;
        double actualShapeWidth = shapeWidth - 2 * padding;
        double actualShapeHeight = shapeHeight - 2 * padding;
        
        // 矩形中心点
        double centerX = actualShapeX + actualShapeWidth / 2;
        double centerY = actualShapeY + actualShapeHeight / 2;
        
        // 定义矩形的四个关键吸附点（边的中点）
        Point2D topPoint = new Point2D(centerX, actualShapeY);                        // 上边中点
        Point2D bottomPoint = new Point2D(centerX, actualShapeY + actualShapeHeight); // 下边中点
        Point2D leftPoint = new Point2D(actualShapeX, centerY);                       // 左边中点
        Point2D rightPoint = new Point2D(actualShapeX + actualShapeWidth, centerY);   // 右边中点
        
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
