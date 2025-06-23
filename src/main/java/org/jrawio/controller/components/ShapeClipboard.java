package org.jrawio.controller.components;

import org.jrawio.controller.shape.Shape;
import org.jrawio.controller.shape.ShapeType;

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
      /**
     * 私有构造函数，确保单例
     */
    private ShapeClipboard() {
        this.items = new ArrayList<>();
    }
    
    /**
     * 获取单例实例
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
     * @param shape 要复制的图形
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
    }
    
    /**
     * 复制多个图形到剪贴板
     * @param shapeList 要复制的图形列表
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
    }
    
    /**
     * 从剪贴板粘贴图形
     * 返回剪贴板中图形的新拷贝
     * @return 粘贴的图形列表
     */
    public List<Shape> paste() {
        if (isEmpty()) {
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
     * 获取剪贴板中的图形列表（只读）
     * @return 图形列表的只读视图
     */
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList<>();
        for (ClipboardItem item : items) {
            shapes.add(item.getShape());
        }
        return Collections.unmodifiableList(shapes);
    }
    
    /**
     * 获取剪贴板中的图形类型列表（只读）
     * @return 图形类型列表的只读视图
     */
    public List<ShapeType> getShapeTypes() {
        List<ShapeType> types = new ArrayList<>();
        for (ClipboardItem item : items) {
            types.add(item.getShapeType());
        }
        return Collections.unmodifiableList(types);
    }
    
    /**
     * 获取剪贴板中的项目列表（只读）
     * @return 项目列表的只读视图
     */
    public List<ClipboardItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    /**
     * 检查剪贴板是否为空
     * @return true如果剪贴板为空，false否则
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    /**
     * 获取剪贴板中图形的数量
     * @return 图形数量
     */
    public int size() {
        return items.size();
    }
    
    /**
     * 清空剪贴板
     */
    public void clear() {
        items.clear();
    }
    
    /**
     * 检查剪贴板中是否包含指定图形
     * @param shape 要检查的图形
     * @return true如果包含，false否则
     */
    public boolean contains(Shape shape) {
        for (ClipboardItem item : items) {
            if (item.getShape().equals(shape)) {
                return true;
            }
        }
        return false;
    }
}
