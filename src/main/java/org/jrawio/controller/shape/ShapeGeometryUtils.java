package org.jrawio.controller.shape;

import javafx.geometry.Point2D;

/**
 * Shape几何计算工具类
 * 提供与图形位置、尺寸计算相关的静态工具方法
 */
public class ShapeGeometryUtils {

    /**
     * 计算八个缩放控制点的位置
     * 
     * @param shapeX      Shape的绘制起始X坐标
     * @param shapeY      Shape的绘制起始Y坐标
     * @param shapeWidth  Shape的绘制宽度
     * @param shapeHeight Shape的绘制高度
     * @param handleSize  控制点的大小
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
     * @param x          检测点的X坐标
     * @param y          检测点的Y坐标
     * @param handleX    控制点的X坐标
     * @param handleY    控制点的Y坐标
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
     * @param totalWidth  Shape的总宽度
     * @param totalHeight Shape的总高度
     * @param padding     内边距
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
     * @param totalWidth  Shape的总宽度
     * @param totalHeight Shape的总高度
     * @param textWidth   文本的宽度
     * @param fontSize    字体大小
     * @return 包含[textX, textY]的数组
     */
    public static double[] calculateCenteredTextPosition(double totalWidth, double totalHeight,
            double textWidth, double fontSize) {
        double textX = totalWidth / 2 - textWidth / 2;
        double textY = totalHeight / 2 + fontSize / 2; // 稍微向下偏移以视觉居中

        return new double[] { textX, textY };
    }

    /**
     * 根据两个点计算边界框
     * 
     * @param point1 第一个点
     * @param point2 第二个点
     * @return 包含[minX, minY, maxX, maxY]的数组
     */
    public static double[] calculateBoundingBox(Point2D point1, Point2D point2) {
        double minX = Math.min(point1.getX(), point2.getX());
        double minY = Math.min(point1.getY(), point2.getY());
        double maxX = Math.max(point1.getX(), point2.getX());
        double maxY = Math.max(point1.getY(), point2.getY());

        return new double[] { minX, minY, maxX, maxY };
    }

    /**
     * 根据边界框计算所需的canvas尺寸
     * 
     * @param boundingBox 边界框 [minX, minY, maxX, maxY]
     * @param padding     边距
     * @param minWidth    最小宽度
     * @param minHeight   最小高度
     * @return 包含[width, height]的数组
     */
    public static double[] calculateCanvasSize(double[] boundingBox, double padding,
            double minWidth, double minHeight) {
        double minX = boundingBox[0];
        double minY = boundingBox[1];
        double maxX = boundingBox[2];
        double maxY = boundingBox[3];

        double width = (maxX - minX) + 2 * padding;
        double height = (maxY - minY) + 2 * padding;

        // 确保最小尺寸
        width = Math.max(width, minWidth);
        height = Math.max(height, minHeight);

        return new double[] { width, height };
    }

    /**
     * 根据边界框和canvas尺寸计算canvas位置
     * 
     * @param boundingBox 边界框 [minX, minY, maxX, maxY]
     * @param padding     边距
     * @return 包含[canvasX, canvasY]的数组
     */
    public static double[] calculateCanvasPosition(double[] boundingBox, double padding) {
        double minX = boundingBox[0];
        double minY = boundingBox[1];

        double canvasX = minX - padding;
        double canvasY = minY - padding;

        return new double[] { canvasX, canvasY };
    }

    /**
     * 将绝对坐标转换为相对于canvas的坐标
     * 
     * @param absolutePoint  绝对坐标点
     * @param canvasPosition canvas位置 [canvasX, canvasY]
     * @return 相对坐标点
     */
    public static Point2D toRelativeCoordinate(Point2D absolutePoint, double[] canvasPosition) {
        return new Point2D(
                absolutePoint.getX() - canvasPosition[0],
                absolutePoint.getY() - canvasPosition[1]);
    }

    /**
     * 计算两点连线的中心点
     * 
     * @param point1 第一个点
     * @param point2 第二个点
     * @return 中心点
     */
    public static Point2D calculateLineCenter(Point2D point1, Point2D point2) {
        return new Point2D(
                (point1.getX() + point2.getX()) / 2.0,
                (point1.getY() + point2.getY()) / 2.0);
    }
}
