package org.jrawio.controller.components;

import org.jrawio.controller.shape.Shape;
import org.jrawio.controller.shape.ShapeType;
import org.jrawio.controller.shape.ShapeFactory;

/**
 * 具体的ShapeCreator实现，基于ShapeType创建形状
 */
public class SimpleShapeCreator implements ShapeCreator {
    private static final long serialVersionUID = 1L;
    
    private final ShapeType shapeType;
    
    public SimpleShapeCreator(ShapeType shapeType) {
        this.shapeType = shapeType;
    }
    
    @Override
    public Shape createShape(double width, double height) {
        return ShapeFactory.createShape(shapeType, width, height);
    }
}
