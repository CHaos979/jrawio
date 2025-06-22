package org.jrawio.controller.components;

import javafx.scene.input.DataFormat;

/**
 * 拖拽相关的常量定义
 */
public final class DragDataFormats {
    
    /**
     * 用于传递ShapeCreator函数式对象的DataFormat
     */
    public static final DataFormat SHAPE_CREATOR_FORMAT = new DataFormat("application/x-jrawio-shape-creator");
    
    // 私有构造函数，防止实例化
    private DragDataFormats() {
        throw new UnsupportedOperationException("常量类不能被实例化");
    }
}
