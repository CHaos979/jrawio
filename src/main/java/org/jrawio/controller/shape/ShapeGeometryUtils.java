package org.jrawio.controller.shape;

/**
 * Shape几何计算工具类
 * 提供与图形位置、尺寸计算相关的静态工具方法
 */
public class ShapeGeometryUtils {
    
    /**
     * 计算八个缩放控制点的位置
     * 
     * @param shapeX Shape的绘制起始X坐标
     * @param shapeY Shape的绘制起始Y坐标
     * @param shapeWidth Shape的绘制宽度
     * @param shapeHeight Shape的绘制高度
     * @param handleSize 控制点的大小
     * @return 包含八个控制点位置的二维数组，每个元素为[x, y]坐标
     */
    public static double[][] calculateResizeHandlePositions(double shapeX, double shapeY, 
            double shapeWidth, double shapeHeight, double handleSize) {
        
        double halfHandle = handleSize / 2;
        
        // 八个控制点的位置
        return new double[][] {
            { shapeX - halfHandle, shapeY - halfHandle }, // TOP_LEFT
            { shapeX + shapeWidth / 2 - halfHandle, shapeY - halfHandle }, // TOP_CENTER
            { shapeX + shapeWidth - halfHandle, shapeY - halfHandle }, // TOP_RIGHT
            { shapeX - halfHandle, shapeY + shapeHeight / 2 - halfHandle }, // MIDDLE_LEFT
            { shapeX + shapeWidth - halfHandle, shapeY + shapeHeight / 2 - halfHandle }, // MIDDLE_RIGHT
            { shapeX - halfHandle, shapeY + shapeHeight - halfHandle }, // BOTTOM_LEFT
            { shapeX + shapeWidth / 2 - halfHandle, shapeY + shapeHeight - halfHandle }, // BOTTOM_CENTER
            { shapeX + shapeWidth - halfHandle, shapeY + shapeHeight - halfHandle } // BOTTOM_RIGHT
        };
    }
    
    /**
     * 检测点是否在控制点区域内
     * 
     * @param x 检测点的X坐标
     * @param y 检测点的Y坐标
     * @param handleX 控制点的X坐标
     * @param handleY 控制点的Y坐标
     * @param handleSize 控制点的大小
     * @return 如果点在控制点区域内返回true，否则返回false
     */
    public static boolean isPointInHandle(double x, double y, double handleX, double handleY, double handleSize) {
        return x >= handleX && x <= handleX + handleSize &&
               y >= handleY && y <= handleY + handleSize;
    }
    
    /**
     * 根据Shape的边界信息计算绘制区域
     * 
     * @param totalWidth Shape的总宽度
     * @param totalHeight Shape的总高度
     * @param padding 内边距
     * @return 包含[x, y, width, height]的数组
     */
    public static double[] calculateDrawingArea(double totalWidth, double totalHeight, double padding) {
        double shapeWidth = totalWidth - 2 * padding;
        double shapeHeight = totalHeight - 2 * padding;
        double x = padding;
        double y = padding;
        
        return new double[] { x, y, shapeWidth, shapeHeight };
    }
    
    /**
     * 计算文本居中显示的位置
     * 
     * @param totalWidth Shape的总宽度
     * @param totalHeight Shape的总高度
     * @param textWidth 文本的宽度
     * @param fontSize 字体大小
     * @return 包含[textX, textY]的数组
     */
    public static double[] calculateCenteredTextPosition(double totalWidth, double totalHeight, 
            double textWidth, double fontSize) {
        double textX = totalWidth / 2 - textWidth / 2;
        double textY = totalHeight / 2 + fontSize / 2; // 稍微向下偏移以视觉居中
        
        return new double[] { textX, textY };
    }
}
