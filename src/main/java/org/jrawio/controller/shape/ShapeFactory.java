package org.jrawio.controller.shape;

/**
 * 形状工厂类
 * 负责创建不同类型的形状实例，支持通过枚举类型或字符串标识符创建形状。
 */
public class ShapeFactory {
    
    /**
     * 根据形状类型创建对应的形状实例
     * 
     * @param shapeType 形状类型枚举
     * @param width 形状宽度
     * @param height 形状高度
     * @return 创建的形状实例
     * @throws IllegalArgumentException 当形状类型不支持时抛出
     */
    public static Shape createShape(ShapeType shapeType, double width, double height) {
        switch (shapeType) {
            case OVAL:
                return new OvalShape(width, height);
            case RECTANGLE:
                return new RectangleShape(width, height);
            case ARROW:
                return new ArrowShape(width, height);
            default:
                throw new IllegalArgumentException("不支持的形状类型: " + shapeType);
        }
    }
}
