package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import java.util.List;

/**
 * 棱形图形实现类
 */
public class DiamondShape extends BlockShape {

    public DiamondShape(double width, double height) {
        super(width, height);
    }

    /**
     * 拷贝构造方法
     * 创建一个与源DiamondShape具有相同属性的新DiamondShape实例
     * 
     * @param source 源DiamondShape对象
     */
    public DiamondShape(DiamondShape source) {
        super(source);
    }

    @Override
    public void drawShape(GraphicsContext gc, double x, double y, double width, double height) {
        // 设置填充颜色为白色
        gc.setFill(Color.WHITE);

        // 设置边框样式
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        // 计算棱形的四个顶点
        double centerX = x + width / 2;
        double centerY = y + height / 2;

        // 棱形的四个顶点坐标
        double[] xPoints = {
                centerX, // 上顶点
                x + width, // 右顶点
                centerX, // 下顶点
                x // 左顶点
        };

        double[] yPoints = {
                y, // 上顶点
                centerY, // 右顶点
                y + height, // 下顶点
                centerY // 左顶点
        };

        // 先填充棱形
        gc.fillPolygon(xPoints, yPoints, 4);

        // 再绘制边框
        gc.strokePolygon(xPoints, yPoints, 4);
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

        // 计算实际棱形区域（不包括padding）
        double actualShapeX = shapeLayoutX + padding;
        double actualShapeY = shapeLayoutY + padding;
        double actualShapeWidth = shapeWidth - 2 * padding;
        double actualShapeHeight = shapeHeight - 2 * padding;

        // 棱形中心点
        double centerX = actualShapeX + actualShapeWidth / 2;
        double centerY = actualShapeY + actualShapeHeight / 2;

        // 向内偏移的比例，使箭头起点稍微偏离边界
        double insetRatio = 0.95; // 5% 向内偏移

        // 根据箭头控制点类型计算棱形边界上的精确连接点（带向内偏移）
        switch (arrowHandle) {
            case ARROW_TOP:
                // 棱形顶部点，向内偏移
                return new javafx.geometry.Point2D(centerX,
                        actualShapeY + (actualShapeHeight / 2) * (1 - insetRatio));

            case ARROW_BOTTOM:
                // 棱形底部点，向内偏移
                return new javafx.geometry.Point2D(centerX,
                        actualShapeY + actualShapeHeight - (actualShapeHeight / 2) * (1 - insetRatio));

            case ARROW_LEFT:
                // 棱形左侧点，向内偏移
                return new javafx.geometry.Point2D(
                        actualShapeX + (actualShapeWidth / 2) * (1 - insetRatio), centerY);

            case ARROW_RIGHT:
                // 棱形右侧点，向内偏移
                return new javafx.geometry.Point2D(
                        actualShapeX + actualShapeWidth - (actualShapeWidth / 2) * (1 - insetRatio), centerY);

            default:
                // 默认返回棱形中心点
                return new javafx.geometry.Point2D(centerX, centerY);
        }
    }

    @Override
    protected List<Point2D> getAllSnapPoints() {
        // 获取图形的布局坐标和尺寸
        double shapeWidth = getWidth();
        double shapeHeight = getHeight();

        // 计算额外空间（用于箭头控制点）
        double arrowHandleOffset = ArrowHandleManager.getArrowHandleOffset();
        double arrowHandleSize = ArrowHandleManager.getArrowHandleSize();
        double extraSpace = arrowHandleOffset + arrowHandleSize;
        double padding = 4 + extraSpace;

        // 计算实际棱形区域（相对于shape本地坐标）
        double actualShapeX = padding;
        double actualShapeY = padding;
        double actualShapeWidth = shapeWidth - 2 * padding;
        double actualShapeHeight = shapeHeight - 2 * padding;

        // 棱形中心点
        double centerX = actualShapeX + actualShapeWidth / 2;
        double centerY = actualShapeY + actualShapeHeight / 2;

        // 定义棱形的四个关键吸附点（四个顶点）
        Point2D topPoint = new Point2D(centerX, actualShapeY); // 上顶点
        Point2D bottomPoint = new Point2D(centerX, actualShapeY + actualShapeHeight); // 下顶点
        Point2D leftPoint = new Point2D(actualShapeX, centerY); // 左顶点
        Point2D rightPoint = new Point2D(actualShapeX + actualShapeWidth, centerY); // 右顶点

        return java.util.Arrays.asList(topPoint, bottomPoint, leftPoint, rightPoint);
    }

    /**
     * 获取图形类型
     * 
     * @return ShapeType.DIAMOND
     */
    @Override
    public ShapeType getShapeType() {
        return ShapeType.DIAMOND;
    }
}
