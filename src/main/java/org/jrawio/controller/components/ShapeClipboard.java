package org.jrawio.controller.components;

import org.jrawio.controller.shape.Shape;
import org.jrawio.controller.shape.ShapeType;
import org.jrawio.controller.shape.ShapeFactory;
import org.jrawio.controller.shape.OvalShape;
import org.jrawio.controller.shape.RectangleShape;
import org.jrawio.controller.shape.ArrowShape;
import javafx.geometry.Point2D;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 图形剪贴板类 - 单例模式
 * 用于管理复制的图形对象列表
 */
@ToString
public class ShapeClipboard {
    /**
     * 剪贴板项目类 - 存储图形和对应的类型
     */
    @Data
    public static class ClipboardItem {
        private final Shape shape;
        private final ShapeType shapeType;
    }

    /** 单例实例 */
    private static ShapeClipboard instance;

    /**
     * 获取单例实例
     * 
     * @return ShapeClipboard实例
     */
    public static ShapeClipboard getInstance() {
        if (instance == null) {
            instance = new ShapeClipboard();
        }
        return instance;
    }

    /** 剪贴板中的图形项目列表 */
    private final List<ClipboardItem> items;

    /** 剪贴板中图形的中心点（所有图形位置的均值） */
    private Point2D centerPoint;

    private ShapeClipboard() {
        this.items = new ArrayList<>();
    }

    /**
     * 复制图形
     */
    public void copy(ClipboardItem item) {
        items.add(item);
        calculateCenterPoint();
    }

    /**
     * 复制多个图形
     * 
     * @param shapes 要复制的图形列表
     */
    public void copy(List<Shape> shapes) {
        items.clear(); // 清空现有剪贴板内容
        for (Shape shape : shapes) {
            ShapeType shapeType = getShapeType(shape);
            if (shapeType != null) {
                items.add(new ClipboardItem(shape, shapeType));
            } else {
                System.out.println(
                        "Warning: Unable to determine shape type for shape: " + shape.getClass().getSimpleName());
            }
        }
        calculateCenterPoint();
        System.out.println("Copied " + items.size() + " shapes to clipboard");
    }

    /**
     * 根据 Shape 实例确定其类型
     * 
     * @param shape Shape 实例
     * @return 对应的 ShapeType，如果无法确定则返回 null
     */
    private ShapeType getShapeType(Shape shape) {
        if (shape instanceof OvalShape) {
            return ShapeType.OVAL;
        } else if (shape instanceof RectangleShape) {
            return ShapeType.RECTANGLE;
        } else if (shape instanceof ArrowShape) {
            return ShapeType.ARROW;
        }
        return null;
    }

    /**
     * 从剪贴板粘贴图形
     * 返回剪贴板中图形的新拷贝
     * 
     * @param point 粘贴目标位置
     * @return 粘贴的图形列表
     */
    public List<Shape> paste(Point2D point) {
        if (items.isEmpty() || point == null) {
            return new ArrayList<>();
        }

        List<Shape> pastedShapes = new ArrayList<>();

        // 计算位移量：目标位置与剪贴板中心点的差值
        double offsetX = 0;
        double offsetY = 0;
        if (centerPoint != null) {
            offsetX = point.getX() - centerPoint.getX();
            offsetY = point.getY() - centerPoint.getY();
        }

        // 使用 ShapeFactory 创建每个图形的拷贝
        for (ClipboardItem item : items) {
            try {
                Shape originalShape = item.getShape();
                ShapeType shapeType = item.getShapeType();

                // 使用工厂方法创建拷贝
                Shape copiedShape = ShapeFactory.createShapeByCopy(shapeType, originalShape);

                // 应用位移，将拷贝的图形移动到目标位置
                copiedShape.setLayoutX(originalShape.getLayoutX() + offsetX);
                copiedShape.setLayoutY(originalShape.getLayoutY() + offsetY);

                pastedShapes.add(copiedShape);
            } catch (Exception e) {
                // 如果某个图形拷贝失败，跳过并继续处理其他图形
                System.err.println("Failed to copy shape: " + e.getMessage());
            }
        }

        return pastedShapes;
    }

    /**
     * 清空剪贴板
     */
    public void clear() {
        items.clear();
        centerPoint = null;
    }

    /**
     * 计算剪贴板中所有图形的中心点（位置均值）
     */
    private void calculateCenterPoint() {
        if (items.isEmpty()) {
            centerPoint = null;
            return;
        }

        double totalX = 0;
        double totalY = 0;
        int count = 0;

        for (ClipboardItem item : items) {
            Shape shape = item.getShape();
            // 计算图形的中心点：layoutX/Y + width/height 的一半
            double shapeCenterX = shape.getLayoutX() + shape.getWidth() / 2.0;
            double shapeCenterY = shape.getLayoutY() + shape.getHeight() / 2.0;

            totalX += shapeCenterX;
            totalY += shapeCenterY;
            count++;
        }

        if (count > 0) {
            double avgX = totalX / count;
            double avgY = totalY / count;
            centerPoint = new Point2D(avgX, avgY);
        } else {
            centerPoint = null;
        }
    }
}
