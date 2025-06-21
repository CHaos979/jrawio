package org.jrawio.controller.shape;

/**
 * Shape工厂类
 */
public class ShapeFactory {
    
    /**
     * 根据形状类型创建对应的Shape实例
     */
    public static Shape createShape(ShapeType shapeType, double width, double height) {
        switch (shapeType) {
            case OVAL:
                return new OvalShape(width, height);
            case RECTANGLE:
                return new RectangleShape(width, height);
            default:
                throw new IllegalArgumentException("不支持的形状类型: " + shapeType);
        }
    }
    
    /**
     * 根据字符串标识符创建对应的Shape实例
     */
    public static Shape createShape(String identifier, double width, double height) {
        for (ShapeType type : ShapeType.values()) {
            if (type.getIdentifier().equals(identifier)) {
                return createShape(type, width, height);
            }
        }
        throw new IllegalArgumentException("不支持的形状标识符: " + identifier);
    }
}
