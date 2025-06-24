package org.jrawio.controller.components;

import javafx.scene.input.DataFormat;

/**
 * 拖拽相关的常量定义
 */
public final class DragDataFormats {

    /**
     * 用于传递形状类型的DataFormat
     */
    public static final DataFormat SHAPE_TYPE_FORMAT = new DataFormat("application/x-jrawio-shape-type");

    /**
     * 用于传递形状宽度的DataFormat
     */
    public static final DataFormat SHAPE_WIDTH_FORMAT = new DataFormat("application/x-jrawio-shape-width");

    /**
     * 用于传递形状高度的DataFormat
     */
    public static final DataFormat SHAPE_HEIGHT_FORMAT = new DataFormat("application/x-jrawio-shape-height");

    // 私有构造函数，防止实例化
    private DragDataFormats() {
        throw new UnsupportedOperationException("常量类不能被实例化");
    }
}
