package org.jrawio.controller.components;

import org.jrawio.controller.shape.Shape;
import org.jrawio.controller.shape.ShapeType;
import org.jrawio.controller.shape.ShapeFactory;

/**
 * 具体的ShapeCreator实现，基于ShapeType和预定义大小创建形状
 */
public class SimpleShapeCreator implements ShapeCreator {
    private static final long serialVersionUID = 1L;
    
    private final ShapeType shapeType;
    private final double predefinedWidth;
    private final double predefinedHeight;
    
    public SimpleShapeCreator(ShapeType shapeType, double width, double height) {
        this.shapeType = shapeType;
        this.predefinedWidth = width;
        this.predefinedHeight = height;
    }
    
    @Override
    public Shape createShape() {
        // 使用预定义的大小，忽略传入的参数
        return ShapeFactory.createShape(shapeType, predefinedWidth, predefinedHeight);
    }
}
