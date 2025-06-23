package org.jrawio.controller.components;

import org.jrawio.controller.shape.Shape;
import org.jrawio.controller.shape.ShapeType;
import javafx.geometry.Point2D;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * 图形剪贴板类 - 单例模式
 * 用于管理复制的图形对象列表
 */
@ToString
public class ShapeClipboard {
    /** 单例实例 */
    private static ShapeClipboard instance;

    /**
     * 剪贴板项目类 - 存储图形和对应的类型
     */
    @Data
    public static class ClipboardItem {
        private final Shape shape;
        private final ShapeType shapeType;
    }

    /** 剪贴板中的图形项目列表 */
    private final List<ClipboardItem> items;

    /** 剪贴板中图形的中心点（所有图形位置的均值） */
    private Point2D centerPoint;

    private ShapeClipboard() {
        this.items = new ArrayList<>();
    }

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

    /**
     * 复制图形到剪贴板
     * 
     * @param shape     要复制的图形
     * @param shapeType 图形类型
     */
    public void copy(Shape shape, ShapeType shapeType) {
        if (shape == null || shapeType == null) {
            return;
        }

        // 清空剪贴板
        clear();
        // 添加图形的拷贝到剪贴板
        Shape copiedShape = shape.copy();
        items.add(new ClipboardItem(copiedShape, shapeType));

        // 计算中心点
        calculateCenterPoint();
    }

    /**
     * 复制多个图形到剪贴板
     * 
     * @param shapeList  要复制的图形列表
     * @param shapeTypes 对应的图形类型列表
     */
    public void copy(List<Shape> shapeList, List<ShapeType> shapeTypes) {
        if (shapeList == null || shapeTypes == null ||
                shapeList.isEmpty() || shapeList.size() != shapeTypes.size()) {
            return;
        }

        // 清空剪贴板
        clear();
        // 添加所有图形的拷贝到剪贴板
        for (int i = 0; i < shapeList.size(); i++) {
            Shape shape = shapeList.get(i);
            ShapeType shapeType = shapeTypes.get(i);
            if (shape != null && shapeType != null) {
                Shape copiedShape = shape.copy();
                items.add(new ClipboardItem(copiedShape, shapeType));
            }
        }

        // 计算中心点
        calculateCenterPoint();
    }

    /**
     * 从剪贴板粘贴图形
     * 返回剪贴板中图形的新拷贝
     * 
     * @return 粘贴的图形列表
     */
    public List<Shape> paste() {
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Shape> pastedShapes = new ArrayList<>();

        // 为剪贴板中的每个图形创建新的拷贝
        for (ClipboardItem item : items) {
            Shape pastedShape = item.getShape().copy();
            pastedShapes.add(pastedShape);
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
