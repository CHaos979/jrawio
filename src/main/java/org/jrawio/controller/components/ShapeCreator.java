package org.jrawio.controller.components;

import org.jrawio.controller.shape.Shape;
import java.io.Serializable;

/**
 * 函数式接口，用于创建形状
 * 实现了Serializable以便在拖拽数据中传递
 */
@FunctionalInterface
public interface ShapeCreator extends Serializable {
    
    /**
     * 创建形状
     * @param width 形状宽度
     * @param height 形状高度
     * @return 创建的形状对象
     */
    Shape createShape(double width, double height);
}
