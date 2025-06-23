package org.jrawio.controller.shape;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

/**
 * 箭头创建管理器
 * 负责处理从BlockShape创建箭头的逻辑
 */
public class ArrowCreationManager {
    
    /**
     * 根据箭头控制点计算箭头的起始点
     * 
     * @param sourceShape 源图形
     * @param arrowHandle 箭头控制点类型
     * @return 箭头起始点的绝对坐标
     */
    public static Point2D calculateArrowStartPoint(BlockShape sourceShape, ArrowHandleManager.ArrowHandle arrowHandle) {
        // 获取源图形的布局坐标和尺寸
        double shapeLayoutX = sourceShape.getLayoutX();
        double shapeLayoutY = sourceShape.getLayoutY();
        double shapeWidth = sourceShape.getWidth();
        double shapeHeight = sourceShape.getHeight();
        
        // 计算额外空间（用于箭头控制点）
        double arrowHandleOffset = ArrowHandleManager.getArrowHandleOffset();
        double arrowHandleSize = ArrowHandleManager.getArrowHandleSize();
        double extraSpace = arrowHandleOffset + arrowHandleSize;
        double padding = 4 + extraSpace;
        
        // 计算实际图形区域（不包括padding）
        double actualShapeX = shapeLayoutX + padding;
        double actualShapeY = shapeLayoutY + padding;
        double actualShapeWidth = shapeWidth - 2 * padding;
        double actualShapeHeight = shapeHeight - 2 * padding;
        
        // 根据箭头控制点类型计算连接点
        switch (arrowHandle) {
            case ARROW_TOP:
                return new Point2D(actualShapeX + actualShapeWidth / 2, actualShapeY);
            case ARROW_BOTTOM:
                return new Point2D(actualShapeX + actualShapeWidth / 2, actualShapeY + actualShapeHeight);
            case ARROW_LEFT:
                return new Point2D(actualShapeX, actualShapeY + actualShapeHeight / 2);
            case ARROW_RIGHT:
                return new Point2D(actualShapeX + actualShapeWidth, actualShapeY + actualShapeHeight / 2);
            default:
                // 默认返回图形中心点
                return new Point2D(actualShapeX + actualShapeWidth / 2, actualShapeY + actualShapeHeight / 2);
        }
    }
    
    /**
     * 创建箭头形状
     * 
     * @param startPoint 起始点（绝对坐标）
     * @param endPoint 结束点（绝对坐标）
     * @return 创建的箭头形状
     */
    public static ArrowShape createArrow(Point2D startPoint, Point2D endPoint) {
        return new ArrowShape(startPoint, endPoint);
    }
    
    /**
     * 将箭头添加到画布容器中
     * 
     * @param arrow 箭头形状
     * @param container 容器（通常是画布的父容器）
     */
    public static void addArrowToContainer(ArrowShape arrow, Pane container) {
        if (container != null && arrow != null) {
            container.getChildren().add(arrow);
        }
    }
    
    /**
     * 获取图形的父容器
     * 
     * @param shape 图形
     * @return 父容器，如果没有则返回null
     */
    public static Pane getShapeContainer(Shape shape) {
        if (shape.getParent() instanceof Pane) {
            return (Pane) shape.getParent();
        }
        return null;
    }
}
