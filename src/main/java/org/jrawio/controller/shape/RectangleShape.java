package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Point2D;
import java.util.List;

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
        // 设置线条宽度
        gc.setLineWidth(1);

        // 绘制矩形（填充背景和边框）
        gc.fillRect(x, y, width, height);
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
    protected List<Point2D> getAllSnapPoints() {
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
        Point2D topPoint = new Point2D(centerX, actualShapeY); // 上边中点
        Point2D bottomPoint = new Point2D(centerX, actualShapeY + actualShapeHeight); // 下边中点
        Point2D leftPoint = new Point2D(actualShapeX, centerY); // 左边中点
        Point2D rightPoint = new Point2D(actualShapeX + actualShapeWidth, centerY); // 右边中点

        return java.util.Arrays.asList(topPoint, bottomPoint, leftPoint, rightPoint);
    }

    /**
     * 获取图形类型
     * 
     * @return ShapeType.RECTANGLE
     */
    @Override
    public ShapeType getShapeType() {
        return ShapeType.RECTANGLE;
    }
}
